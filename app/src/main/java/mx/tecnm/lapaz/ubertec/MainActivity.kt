package mx.tecnm.lapaz.ubertec

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val platillosFragment = PlatillosFragment.newInstance("","")
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainerView,platillosFragment,"platillos")
            .commit()
        pedirPermisonCamara()
    }

    fun cambarAlFractoLogin()
    {
        val loginFragment = LoginFragment.newInstance("","")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView,loginFragment,"login")
            .commit()
    }

    val REQUEST_IMAGE_CAPTURE = 124
    val REQUEST_TAKE_PHOTO = 123
    val MY_PERMISSIONS_REQUEST_CAMERA = 100
    val CAMERA_PREF = "camera_pref"
    val ALLOW_KEY = "ALLOWED"
    lateinit var currentPhotoPath: String

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val path = getFromPrefString(this, "UltimaFoto")
            val id = getFromPrefString(this, "id")
            CoroutineScope(Job()).launch {
                WebServiceREST.postFile(
                    "http://192.168.0.23/restaurant/public/subirFoto",
                    listOf<Parametro>(Parametro("id", id.toString())),
                    "foto",
                    "$id.jpg",
                    FileInputStream(path)
                )
            }
            //val tvDirectorio = findViewById<TextView>(R.id.tvDirectorio)
            //tvDirectorio.text = path
            //val imageBitmap = data?.extras?.get("data") as Bitmap
            //val imageBitmap = BitmapFactory.decodeFile(path)
            //val ivFoto = findViewById<ImageView>(R.id.ivFoto)
            //ivFoto.setImageBitmap(imageBitmap)
            //galleryAddPic()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA
            )
        ) {
            val photoFile = createImageFile()
            saveToPreferences(this,"UltimaFoto",photoFile.path)
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "mx.tecnm.lapaz.ubertec.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
    //Guardar preferencia /////////////
    fun saveToPreferences(context: Context, key: String, texto: String) {
        val myPrefs = context.getSharedPreferences(CAMERA_PREF, Context.MODE_PRIVATE)
        val prefsEditor = myPrefs.edit()
        prefsEditor.putString(key, texto)
        prefsEditor.commit()
    }

    fun getFromPref(context: Context, key: String): String? {
        val myPrefs = context.getSharedPreferences(
            CAMERA_PREF,
            Context.MODE_PRIVATE
        )
        return myPrefs.getString(key, "false")
    }

    fun getFromPrefString(context: Context, key: String): String? {
        val myPrefs = context.getSharedPreferences(CAMERA_PREF, Context.MODE_PRIVATE)
        return myPrefs.getString(key, "")
    }
    ////////////////////////////////////
//SOLICITUD DE PERMISOS ////////////
    fun pedirPermisonCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)=="true") {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                var i = 0
                val len = permissions.size
                while (i < len) {
                    val permission = permissions[i]
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            this, permission
                        )

                        if (showRationale) {
                            showAlert()
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(this, ALLOW_KEY, "true")
                        }
                    }
                    i++
                }
            }
        }
    }

    private fun showAlert() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("App needs to access the Camera.")

        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                finish()
            })

        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "ALLOW",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
            })
        alertDialog.show()
    }

    private fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("App needs to access the Camera.")

        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                //finish();
            })

        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "SETTINGS",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                startInstalledAppDetailsActivity(this)
            })

        alertDialog.show()
    }

    fun startInstalledAppDetailsActivity(context: Activity?) {
        if (context == null) {
            return
        }
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + context.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(i)
    }
    ////////////////////////////////////
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }
}