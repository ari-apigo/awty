package edu.us.ischool.arewethereyet

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    lateinit var messageET: EditText
    lateinit var phoneNumET: EditText
    lateinit var minutesET: EditText
    lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageET = findViewById(R.id.etMessage)
        phoneNumET = findViewById(R.id.etPhoneNum)
        minutesET = findViewById(R.id.etMinutes)

        val startBtn = findViewById<Button>(R.id.btnStart)
        startBtn.setOnClickListener {
            // only start when all fields have legitimate values
            if (legitimateFields() && startBtn.text.equals("Start")) {
                startBtn.text = "Stop"
                startMessages(startBtn.context, phoneNumET.text.toString(), messageET.text.toString(), minutesET.text.toString().toInt())
            } else if (startBtn.text.equals("Stop")) {
                startBtn.text = "Start"
                timer.cancel()
                Log.i("Stopped messages", "No more messages coming.")
            } else if (!legitimateFields()) {
                Toast.makeText(this, "One or more inputs are invalid.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun legitimateFields(): Boolean {
        // legit message is not empty
        val legitMessage = !TextUtils.isEmpty(messageET.text.toString())

        // legit phone number is 10 digits
        //val legitPhoneNum = phoneNumET.text.length == 10

        // legit minutes is not 0, positive, and a whole number
        val minutes = minutesET.text.toString().toInt()
        val legitMinutes = minutes > 0

        //return legitMessage && legitPhoneNum && legitMinutes
        return legitMessage && legitMinutes
    }

    fun startMessages(c: Context, to: String, m: String, n: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval = (n * 60000).toLong()
        val time = System.currentTimeMillis() + interval
        val intent = Intent("Placeholder").apply {
            //Toast.makeText(c, "$to: $m", Toast.LENGTH_SHORT).show()
            //Log.i("Incoming message", "A message has been sent at $time. Another coming in $interval ms.")
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, pendingIntent)

        // split phone number to format correctly
        // 5551234567
        // 0123456789
//        val to1 = to.substring(0, 3)
//        val to2 = to.substring(3, 6)
//        val to3 = to.substring(6)

        // check for permission to send sms
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.SEND_SMS), 100)
        } else {
            // start sending sms every specified interval
            timer = fixedRateTimer("timer", false, 0, interval){
                this@MainActivity.runOnUiThread{
//                    Toast.makeText(c, "($to1) ${to2}-${to3}: $m", Toast.LENGTH_SHORT).show()
//                    Log.i("Incoming message", "A message has been sent. Another coming in $interval ms.")

                    // part 2
                    val smsMgr = getSystemService(SmsManager::class.java)
                    smsMgr.sendTextMessage(to, null, m, null, null)
                }
            }
        }

        // consulted https://stackoverflow.com/questions/45287818/handler-to-run-task-every-5-seconds-kotlin
//        timer = fixedRateTimer("timer", false, 0, interval){
//            this@MainActivity.runOnUiThread{
//                Toast.makeText(c, "($to1) ${to2}-${to3}: $m", Toast.LENGTH_SHORT).show()
//                Log.i("Incoming message", "A message has been sent. Another coming in $interval ms.")
//            }
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}