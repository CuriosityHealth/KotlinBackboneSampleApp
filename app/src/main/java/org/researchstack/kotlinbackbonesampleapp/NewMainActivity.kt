package org.researchstack.kotlinbackbonesampleapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.researchstack.backbone.StorageAccess
import org.researchstack.backbone.interfaces.*
import org.researchstack.backbone.result.TaskResult
import org.researchstack.backbone.task.Task
import org.researchstack.backbone.ui.BackwardsCompatibleStepFragmentProvider
import org.researchstack.backbone.ui.PinCodeActivity
import org.researchstack.backbone.ui.ViewTaskMultiFragment
import org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout

class NewMainActivity : PinCodeActivity(), ITaskPresenterDelegate {

    var mainFragment: MainFragment? = null
    var viewTaskFragment: ViewTaskMultiFragment? = null


    private var taskProvider: TaskProvider? = null

    private var stepFragmentProvider: IStepFragmentProvider = {
        val stepLayoutProvider = BackwardsCompatibleStepLayoutProvider()
        BackwardsCompatibleStepFragmentProvider(stepLayoutProvider)
    }()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)

//        val toolbar = findViewById(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        this.supportActionBar!!.setDisplayShowTitleEnabled(true)

        this.taskProvider = TaskProvider(this)


        val mainFragment = MainFragment()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(org.researchstack.backbone.R.anim.rsb_slide_in_up, org.researchstack.backbone.R.anim.rsb_slide_out_down)
                .add(R.id.new_main_container, mainFragment, "MainFragment")
                .commit()

        supportFragmentManager.executePendingTransactions()

        mainFragment.launchConsentHandler = {
            launchTask(TaskProvider.CONSENT)
        }

        mainFragment.launchSurveyHandler = {
            launchTask(TaskProvider.SAMPLE_SURVEY)
        }

        this.mainFragment = mainFragment

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_clear) {
            clearData()
            Toast.makeText(this, R.string.menu_data_cleared, Toast.LENGTH_SHORT).show()
            return true
        }
        else if (item.itemId == android.R.id.home) {
            this.viewTaskFragment?.onBackPressed()
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun clearData() {
        val appPrefs = AppPrefs.getInstance(this)
        appPrefs.setHasSurveyed(false)
        appPrefs.setHasConsented(false)

        this.mainFragment?.initViews(this)
    }

//    override fun onDataReady() {
//        super.onDataReady()
////        initViews()
//    }

    private fun launchTask(identifier: String) {

        val newFragment = ViewTaskMultiFragment.newInstance(identifier) as ViewTaskMultiFragment
        newFragment.taskProvider = this.taskProvider!!
        newFragment.stepFragmentProvider = this.stepFragmentProvider
        newFragment.setTaskPresenterDelegate(this)
        this.viewTaskFragment = newFragment

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(org.researchstack.backbone.R.anim.rsb_slide_in_up, org.researchstack.backbone.R.anim.rsb_slide_out_down)
                .replace(R.id.new_main_container, newFragment, identifier)
                .commit()

        supportFragmentManager.executePendingTransactions()

        newFragment.startPresenting()
    }

    override fun didFinishPresenting(task: ITask, result: IResult?) {

        if (task.identifier == TaskProvider.SAMPLE_SURVEY) {
            (result as? TaskResult)?.let {
                this.processSurveyResult(it)
            }
        }

        if (task.identifier == TaskProvider.CONSENT) {
            (result as? TaskResult)?.let {
                this.processConsentResult(it)
            }
        }

        val mainFragment = this.mainFragment
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(org.researchstack.backbone.R.anim.rsb_slide_in_up, org.researchstack.backbone.R.anim.rsb_slide_out_down)
                .replace(R.id.new_main_container, mainFragment, "MainFragment")
                .commit()

        this.viewTaskFragment = null
//        val mainFragment: MainFragment = supportFragmentManager.findFragmentByTag("MainFragment") as MainFragment
//        val mainFragment = this.mainFragment
//
//        supportFragmentManager.beginTransaction()
//                .setCustomAnimations(org.researchstack.backbone.R.anim.rsb_slide_in_up, org.researchstack.backbone.R.anim.rsb_slide_out_down)
//                .replace(R.id.new_main_container, mainFragment, "MainFragment")
//                .commit()
//
//        supportFragmentManager.executePendingTransactions()

    }

    override fun didStartPresenting(task: ITask) {

    }

    private fun processSurveyResult(result: TaskResult) {
        StorageAccess.getInstance().appDatabase.saveTaskResult(result)

        val prefs = AppPrefs.getInstance(this)
        prefs.setHasSurveyed(true)
//        initViews()
    }

    private fun processConsentResult(result: TaskResult) {
        val consented = result.getStepResult(MainActivity.CONSENT_DOC).result as Boolean

        if (consented) {
            StorageAccess.getInstance().appDatabase.saveTaskResult(result)

            val prefs = AppPrefs.getInstance(this)
            prefs.setHasConsented(true)

//            initViews()
        }
    }

//    private fun initViews() {
//        val prefs = AppPrefs.getInstance(this)
//
//        val lblConsentedDate = findViewById(R.id.consented_date_lbl) as TextView
//        val consentedDate = findViewById(R.id.consented_date) as TextView
//        val consentedSig = findViewById(R.id.consented_signature) as ImageView
//
//        if (prefs.hasConsented()) {
//            this.consentButton!!.setVisibility(View.GONE)
//            this.surveyButton!!.setEnabled(true)
//
//            consentedSig.visibility = View.VISIBLE
//            consentedDate.visibility = View.VISIBLE
//            lblConsentedDate.setVisibility(View.VISIBLE)
//
//            printConsentInfo(consentedDate, consentedSig)
//        } else {
//            this.consentButton!!.setVisibility(View.VISIBLE)
//            this.surveyButton!!.setEnabled(false)
//
//            consentedSig.visibility = View.INVISIBLE
//            consentedSig.setImageBitmap(null)
//            consentedDate.visibility = View.INVISIBLE
//            lblConsentedDate.setVisibility(View.INVISIBLE)
//        }
//
//        val surveyAnswer = findViewById(R.id.survey_results) as TextView
//
//        if (prefs.hasSurveyed()) {
//            surveyAnswer.visibility = View.VISIBLE
//            printSurveyInfo(surveyAnswer)
//        } else {
//            surveyAnswer.visibility = View.GONE
//        }
//    }
//
//    private fun printSurveyInfo(surveyAnswer: TextView) {
//        val taskResult = StorageAccess.getInstance()
//                .appDatabase
//                .loadLatestTaskResult(MainActivity.SAMPLE_SURVEY)
//
//        var results = ""
//        for (id in taskResult.results.keys) {
//            val stepResult = taskResult.getStepResult(id)
//            results += id + ": " + stepResult.result.toString() + "\n"
//        }
//
//        surveyAnswer.text = results
//    }
//
//    private fun printConsentInfo(consentedDate: TextView, consentedSig: ImageView) {
//        val result = StorageAccess.getInstance()
//                .appDatabase
//                .loadLatestTaskResult(MainActivity.CONSENT)
//
//        val signatureBase64 = result.getStepResult(MainActivity.SIGNATURE)
//                .getResultForIdentifier(ConsentSignatureStepLayout.KEY_SIGNATURE) as String
//
//        val signatureDate = result.getStepResult(MainActivity.SIGNATURE)
//                .getResultForIdentifier(ConsentSignatureStepLayout.KEY_SIGNATURE_DATE) as String
//
//        consentedDate.text = signatureDate
//
//        val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
//        consentedSig.setImageBitmap(BitmapFactory.decodeByteArray(
//                signatureBytes,
//                0,
//                signatureBytes.size))
//    }
}