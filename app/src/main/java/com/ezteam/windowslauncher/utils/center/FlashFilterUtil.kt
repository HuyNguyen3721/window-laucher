package com.ezteam.windowslauncher.utils.center

import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import com.ezteam.baseproject.utils.PreferencesUtils

object FlashFilterUtil {
    private var camera: Camera? = null
    private var parameters: Camera.Parameters? = null
    private var cameraManager: CameraManager? = null

    fun turnFlashlightOn(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (cameraManager == null) {
                    cameraManager =
                        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                }

                if (cameraManager != null) {
                    val cameraId = try {
                        cameraManager!!.cameraIdList[0] ?: "0"
                    } catch (e: Exception) {
                        "0"
                    }
                    cameraManager!!.setTorchMode(cameraId, true)
                }
            } catch (e: CameraAccessException) {
            } catch (ex: IllegalArgumentException) {
            }
        } else {
            setUpCamera()
            parameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera?.parameters = parameters
            camera?.startPreview()
        }
    }

    fun turnFlashlightOff(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (cameraManager == null) {
                    cameraManager =
                        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                }
                val cameraId: String
                if (cameraManager != null) {
                    try {
                        cameraId = cameraManager!!.cameraIdList[0]
                            ?: "0"// Usually front camera is at 0 position.
                        cameraManager!!.setTorchMode(cameraId, false)
                    } catch (e: Exception) {
                    }
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            } catch (e: java.lang.IllegalArgumentException) {
                e.printStackTrace()
            }
        } else {
            // release camera
            releaseCamera()
            // set up camera
            setUpCamera()
            parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera?.parameters = parameters
            camera?.stopPreview()
            camera?.release()
            camera = null
            parameters = null
        }
    }

    private fun setUpCamera() {
        if (camera == null) {
            try {
                camera = Camera.open()
                parameters = camera?.parameters
            } catch (re: RuntimeException) {
            }
        }
    }

    private fun releaseCamera() {
        if (camera != null) {
            camera?.release()
            camera = null
        }
    }
//    fun turnFlashlightOff(context: Context) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            try {
//                val cameraId: String
//                val cameraManager =
//                    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//                //
//                cameraId =
//                    cameraManager.cameraIdList[0] // Usually front camera is at 0 position.
//                cameraManager.setTorchMode(cameraId, false)
//            } catch (e: CameraAccessException) {
//                e.printStackTrace()
//            } catch (e: IllegalArgumentException) {
//                e.printStackTrace()
//            } catch (e: ArrayIndexOutOfBoundsException) {
//                e.printStackTrace()
//            }
//        } else {
//            releaseCamera()
//            if (camera == null) {
//                try {
//                    camera = Camera.open()
//                } catch (ex: Exception) {
//                }
//            }
//            parameters = camera?.parameters
//            parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
//            camera?.parameters = parameters
//            camera?.stopPreview()
//            camera?.release()
//            camera = null
//            parameters = null
//            //
//        }
//    }
//
//    fun turnFlashlightOn(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            try {
//                val cameraManager =
//                    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//                var cameraId: String? = null
//                cameraId =
//                    if (cameraManager.cameraIdList[0] == null) "0" else cameraManager.cameraIdList[0]
//                cameraManager.setTorchMode(cameraId!!, true)
//            } catch (e: CameraAccessException) {
//            } catch (e: IllegalArgumentException) {
//                e.printStackTrace()
//            }
//        } else {
//            //
//            camera = Camera.open()
//            parameters = camera!!.parameters
//            parameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
//            camera?.parameters = parameters
//            camera?.startPreview()
//        }
//    }
//
//    private fun releaseCamera() {
//        if (camera != null) {
//            camera?.release()
//            camera = null
//        }
//    }
}
