package com.sitegist.ticketvalidation

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.pixplicity.easyprefs.library.Prefs
import com.sitegist.ticketvalidation.data.QRcode
import com.sitegist.ticketvalidation.data.Ticket
import com.sitegist.ticketvalidation.services.ValidationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class ScannerFragment : Fragment() {

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                    view?.findViewById<androidx.camera.view.PreviewView>(R.id.previewView)?.surfaceProvider
                )
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        extractTextFromImage(imageProxy, onSuccess = { qrCodeText ->
            if (qrCodeText.isNotEmpty()) {
                // Parsing QR-code
                val qrCode = parseJsonResponse(qrCodeText)
                if (qrCode != null) {
                    closeCamera()

                    // Send validation request
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://tickets.sitegist.net/")  // Базова URL-адреса
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(ValidationService::class.java)

                    val token = Prefs.getString("preference_token")
                    val authHeader = "Bearer $token"
                    val orderNumber = qrCode.order

                    val call = apiService.checkOrder(orderNumber, authHeader)

                    call.enqueue(object : Callback<Ticket> {
                        override fun onResponse(call: Call<Ticket>, response: Response<Ticket>) {
                            if (response.isSuccessful) {
                                val data = response.body()
                                val mainFragment = MainFragment()
                                val bundle = Bundle()
                                bundle.putParcelable("ticket", data)
                                mainFragment.arguments = bundle

                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, mainFragment)
                                    .addToBackStack(null)
                                    .commit()

                            } else {
                                Log.d(TAG, "onResponse: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Ticket>, t: Throwable) {
                            Log.d(TAG, "onFailure: ${t.message}")
                        }
                    })
                }
            }
        }, onFailure = { exception ->
            exception.printStackTrace()
        })
    }

    private fun closeCamera() {
        cameraProvider.unbindAll()
    }

    private fun parseJsonResponse(jsonString: String): QRcode? {
        return try {
            val gson = Gson()
            gson.fromJson(jsonString, QRcode::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    @OptIn(ExperimentalGetImage::class)
    fun extractTextFromImage(
        imageProxy: ImageProxy, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        val options =
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image).addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val qrCode = barcodes[0]
                val qrCodeText = qrCode.rawValue

                if (qrCodeText != null) {
                    onSuccess(qrCodeText)
                } else {
                    onFailure(Exception("Розпізнано QR-код, але він порожній."))
                }
            } else {
                onFailure(Exception("QR-код не знайдено на зображенні."))
            }
        }.addOnFailureListener { e ->
            onFailure(e)
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    }
}
