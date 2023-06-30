package com.example.drawingapp
//note: has to add active color shit and background color shit to it
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.random.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var drawingview: Drawingview
    private lateinit var selectbrush: ImageButton
    private lateinit var undobutton: ImageButton
    private lateinit var savebutton: ImageButton
    private lateinit var colorbutton: ImageButton
    private lateinit var attachbutton: ImageButton
    private lateinit var red_button: ImageButton
    private lateinit var blue_button: ImageButton
    private lateinit var green_button: ImageButton
    private lateinit var purple_button: ImageButton
    private lateinit var orange_button: ImageButton
    private lateinit var eraser_button: ImageButton

    val OpenGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            findViewById<ImageView>(R.id.attached_img).setImageURI(result.data?.data)
        }

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted && permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    Toast.makeText(this, "Permission $permissionName Granted", Toast.LENGTH_SHORT).show()
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    OpenGalleryLauncher.launch(pickIntent)
                } else if (isGranted && (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "Permission $permissionName Granted", Toast.LENGTH_SHORT).show()
                    CoroutineScope(IO).launch{
                        saveImage(getBitmapfromView(findViewById(R.id.layout_img_tosave)))
                    }

                }
                else {
                    if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE || permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this,
                            "Permission $permissionName Denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingview = findViewById(R.id.drawingview)
        selectbrush = findViewById(R.id.Select_brush)
        undobutton = findViewById(R.id.undobutton)
        attachbutton = findViewById(R.id.attachbutton)
        colorbutton = findViewById(R.id.color)
        savebutton = findViewById(R.id.savebutton)
        red_button = findViewById(R.id.red_button)
        eraser_button = findViewById(R.id.eraser_button)
        blue_button = findViewById(R.id.blue_button)
        green_button = findViewById(R.id.green_button)
        purple_button = findViewById(R.id.purple_button)
        orange_button = findViewById(R.id.orange_button)


        selectbrush.setOnClickListener(this)
        red_button.setOnClickListener(this)
        blue_button.setOnClickListener(this)
        orange_button.setOnClickListener(this)
        green_button.setOnClickListener(this)
        purple_button.setOnClickListener(this)
        eraser_button.setOnClickListener(this)
        undobutton.setOnClickListener(this)
        savebutton.setOnClickListener(this)
        attachbutton.setOnClickListener(this)
        colorbutton.setOnClickListener(this)

    }

    fun showBrushchooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.brush_size)
        val brushsize: SeekBar = brushDialog.findViewById(R.id.brush_size_seek)
        var brushsizeTv: TextView = brushDialog.findViewById(R.id.brush_size_text)


        brushsize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                drawingview.changeBrushSize(seekBar.progress.toFloat())
                brushsizeTv.text = brushsize.progress.toString()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        brushDialog.show()
    }

    fun showcolorpickerDialog() {
        var Dialog = AmbilWarnaDialog(this, Color.RED, object : OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingview.changecolor(color)
            }

        })
        Dialog.show()

    }

    private fun requeststoragepermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        ) {
            showpermiossionDialog()
        } else {
            requestPermission.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )

        }
    }

    private fun showpermiossionDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Storage Permission")
        dialog.setMessage("Need this permission to access media from galllery")

        dialog.setPositiveButton(R.string.dialog_yes) { dialog, _ ->
            requestPermission.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            dialog.dismiss()

        }
        dialog.create().show()
    }

    //converting layout that contain drawng to img
    private fun getBitmapfromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap

        //this here make a bitmap of a size view here i.e our contraint layout containing drawing board
        //and make it into canvas and we
    }

    //saveimg

    private suspend fun saveImage(bitmap: Bitmap) {
        //telling to store img in public directory in pictures
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() //external storage directory is stored in root
        val myDir =
            File("$root/saved_Images")  //new dir is created with this name using mkdir() command
        myDir.mkdir()
        val generator = java.util.Random()
        var n = 1000
        n = generator.nextInt(n) //random number generated to name img
        var outputFile = File(myDir, "Images-$n.jpg")  //output file is created
        if (outputFile.exists()) {
            outputFile.delete()
        } else {
            try {
                val out = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.stackTrace

            }

            //using coroutines
            withContext(Main) {
                Toast.makeText(
                    this@MainActivity,
                    "${outputFile.absolutePath} Saved!!",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.Select_brush -> {
                showBrushchooserDialog()
            }

            R.id.red_button -> {
                drawingview.changecolor("#FD291A")
            }

            R.id.orange_button -> {
                drawingview.changecolor("#FF9800")
            }

            R.id.green_button -> {
                drawingview.changecolor("#89FF00")
            }

            R.id.purple_button -> {
                drawingview.changecolor("#9C27B0")
            }

            R.id.blue_button -> {
                drawingview.changecolor("#008EFF")
            }

            R.id.eraser_button -> {
                drawingview.changecolor("#fafafa")
            }

            R.id.savebutton -> {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requeststoragepermission()
                } else {
                    val layout = findViewById<ConstraintLayout>(R.id.layout_img_tosave)
                    val bitmap = getBitmapfromView(layout)
                    CoroutineScope(IO).launch {
                        saveImage(bitmap)
                    }

                }
            }

            R.id.attachbutton -> {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requeststoragepermission()
                } else {
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    OpenGalleryLauncher.launch(pickIntent)
                }
            }


            R.id.color -> {
                showcolorpickerDialog()
            }

            R.id.undobutton -> {
                drawingview.Undo()
            }

        }
    }


}