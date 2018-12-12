package org.researchstack.kotlinbackbonesampleapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.os.CancellationSignal
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.researchstack.feature.authentication.pincode.PasscodeAuthenticator
import org.researchstack.feature.authentication.pincode.step.PassCodeCreationStep
import org.researchstack.feature.authentication.pincode.ui.PasscodeAuthenticationFragment
import org.researchstack.feature.storage.StorageAccess
import org.researchstack.feature.storage.file.StorageAccessListener
import org.researchstack.foundation.components.common.ui.activities.ViewTaskActivity
import org.researchstack.feature.consent.model.ConsentDocument
import org.researchstack.feature.consent.model.ConsentSection
import org.researchstack.feature.consent.model.ConsentSignature
import org.researchstack.feature.consent.step.ConsentDocumentStep
import org.researchstack.feature.consent.step.ConsentSignatureStep
import org.researchstack.feature.consent.step.ConsentVisualStep
import org.researchstack.feature.consent.ui.layout.ConsentSignatureStepLayout
import org.researchstack.foundation.components.survey.answerformat.*
import org.researchstack.foundation.components.survey.model.Choice
import org.researchstack.foundation.components.survey.step.FormStep
import org.researchstack.foundation.components.survey.step.InstructionStep
import org.researchstack.foundation.components.survey.step.QuestionStep
import org.researchstack.foundation.components.common.task.OrderedTask
import org.researchstack.foundation.components.utils.LogExt
import org.researchstack.foundation.core.models.result.TaskResult
import org.researchstack.foundation.core.models.task.Task

import java.util.ArrayList

class MainActivity : AppCompatActivity(), StorageAccessListener, PasscodeAuthenticator.PresentationDelegate {

    companion object {

        val TAG = MainActivity.javaClass.name
        // Activity Request Codes
        val REQUEST_CONSENT = 0
        val REQUEST_SURVEY = 1
        val REQUEST_PIN_CODE = 2

        val PIN_CODE = "pin_code"
    }

    // Views
    private var consentButton: AppCompatButton? = null
    private var surveyButton: AppCompatButton? = null

    val taskProvider = FoundationTaskProvider(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        this.supportActionBar!!.setDisplayShowTitleEnabled(true)

        this.consentButton = findViewById(R.id.consent_button) as AppCompatButton
        this.consentButton!!.setOnClickListener {
            launchConsent()
        }

        this.surveyButton = findViewById(R.id.survey_button) as AppCompatButton
        this.surveyButton!!.setOnClickListener {
            launchSurvey()
        }

        val passcodeAuthenticator: PasscodeAuthenticator = BackboneApplication.instance!!.passcodeAuthenticator!!
        passcodeAuthenticator.setPresentationDelegate(this)
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
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun clearData() {
        val appPrefs = AppPrefs.getInstance(this)
        appPrefs.setHasSurveyed(false)
        appPrefs.setHasConsented(false)

        initViews()
    }

    override fun onDataReady() {
        this.storageAccessUnregister()
        initViews()

//        supportFragmentManager.findFragmentByTag("passcode_fragment")?.let { fragment ->
//            supportFragmentManager.beginTransaction()
//                    .setCustomAnimations(R.anim.rsf_slide_in_up, R.anim.rsf_slide_out_down)
//                    .remove(fragment)
//                    .commit()
//        }
    }


    override fun onDataAuth() {
//        this.storageAccessUnregister()
//
//        //TODO: Present Passcode
//        val passcodeAuthenticator: PasscodeAuthenticator = BackboneApplication.instance!!.passcodeAuthenticator!!
//        passcodeAuthenticator.setPresentationDelegate(this)
//
//        val cancelationSignal = CancellationSignal()
//        cancelationSignal.setOnCancelListener {
//
//        }
//
//        val callback = object: PasscodeAuthenticator.PasscodeAuthenticationCallback() {
//            override fun onAuthenticationSucceeded(result: PasscodeAuthenticator.PasscodeAuthenticationResult) {
//                requestStorageAccess()
//            }
//        }
//
//        passcodeAuthenticator.authenticate(cancelationSignal, callback)

    }

    override fun presentPasscodeAuthentication(authenticator: PasscodeAuthenticator, cancel: CancellationSignal, callback: PasscodeAuthenticator.PasscodeAuthenticationCallback) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        //present pin code fragment

        val frame = findViewById<View>(R.id.authorization_container)
        frame.visibility = View.VISIBLE

        val context = this as Context

        val fragmentCallback = object: PasscodeAuthenticationFragment.AuthenticationCallback() {
            override fun onAuthenticationFailed() {
                callback.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded() {
                callback.onAuthenticationSucceeded(PasscodeAuthenticator.PasscodeAuthenticationResult())

                supportFragmentManager.findFragmentByTag("passcode_fragment")?.let { fragment ->
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.rsf_slide_in_up, R.anim.rsf_slide_out_down)
                            .remove(fragment)
                            .commit()
                }

                //need to request storage access again here!!
                StorageAccess.getInstance().requestStorageAccess(context)
            }
        }

        val passcodeFragment = PasscodeAuthenticationFragment.newInstance(authenticator, fragmentCallback)
        val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.authorization_container, passcodeFragment, "passcode_fragment")
                .commit()

        supportFragmentManager.executePendingTransactions()

//        val fragment: ViewTaskFragment = supportFragmentManager.findFragmentByTag(this.task.identifier) as ViewTaskFragment
//        fragment.taskProvider = this
//        fragment.stepLayoutProvider = BackwardsCompatibleStepLayoutProvider()
//        fragment.setTaskPresenterDelegate(this)
//        this.viewTaskFragment = fragment
    }

    override fun onDataFailed() {
        this.storageAccessUnregister()
    }

    fun requestStorageAccess() {
        LogExt.i(TAG, "requestStorageAccess()")
        val storageAccess = StorageAccess.getInstance()
        storageAccessRegister()
        storageAccess.requestStorageAccess(this)
    }

    fun storageAccessRegister() {
        LogExt.i(TAG, "storageAccessRegister()")
        val storageAccess = StorageAccess.getInstance()
        storageAccess.register(this)
    }

    fun storageAccessUnregister() {
        LogExt.i(TAG, "storageAccessUnregister()")
        val storageAccess = StorageAccess.getInstance()
        storageAccess.unregister(this)
    }

    override fun onResume() {
        super.onResume()

        val storageAccess = StorageAccess.getInstance()
        if (!storageAccess.hasPinCode(this)) {
            this.launchPinCodeRegistration()
        }
        else {
            this.requestStorageAccess()
        }

    }

    private fun launchPinCodeRegistration() {
        val task = this.taskProvider.task(FoundationTaskProvider.PIN_CODE)!! as Task

        // Create an activity using the task and set a delegate.
        val intent = ViewTaskActivity.newIntent(this, task)
        startActivityForResult(intent, REQUEST_PIN_CODE)

    }

    private fun processPinCodeResult(result: TaskResult) {
        val pinCode: String = result.getStepResult(PIN_CODE)!!.result as String
        val storageAccess = StorageAccess.getInstance()
        storageAccess.createPinCode(this, pinCode)
    }

    private fun initViews() {
        val prefs = AppPrefs.getInstance(this)

        val lblConsentedDate = findViewById(R.id.consented_date_lbl) as TextView
        val consentedDate = findViewById(R.id.consented_date) as TextView
        val consentedSig = findViewById(R.id.consented_signature) as ImageView

        if (prefs.hasConsented()) {
            this.consentButton!!.setVisibility(View.GONE)
            this.surveyButton!!.setEnabled(true)

            consentedSig.visibility = View.VISIBLE
            consentedDate.visibility = View.VISIBLE
            lblConsentedDate.setVisibility(View.VISIBLE)

            printConsentInfo(consentedDate, consentedSig)
        } else {
            this.consentButton!!.setVisibility(View.VISIBLE)
            this.surveyButton!!.setEnabled(false)

            consentedSig.visibility = View.INVISIBLE
            consentedSig.setImageBitmap(null)
            consentedDate.visibility = View.INVISIBLE
            lblConsentedDate.setVisibility(View.INVISIBLE)
        }

        val surveyAnswer = findViewById(R.id.survey_results) as TextView

        if (prefs.hasSurveyed()) {
            surveyAnswer.visibility = View.VISIBLE
            printSurveyInfo(surveyAnswer)
        } else {
            surveyAnswer.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {
            if (requestCode == REQUEST_CONSENT && resultCode == Activity.RESULT_OK) {
                processConsentResult(it.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult)
            } else if (requestCode == REQUEST_SURVEY && resultCode == Activity.RESULT_OK) {
                processSurveyResult(it.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult)
            }
            else if (requestCode == REQUEST_PIN_CODE && resultCode == Activity.RESULT_OK) {
                processPinCodeResult(it.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult)
            }
        }

    }

    // Consent stuff

    private fun launchConsent() {
        val consentTask = this.taskProvider.task(FoundationTaskProvider.CONSENT)!! as Task

        // Launch using hte ViewTaskActivity and make sure to listen for the activity result
        val intent = ViewTaskActivity.newIntent(this, consentTask)
        startActivityForResult(intent, REQUEST_CONSENT)
    }

    private fun processConsentResult(result: TaskResult) {
        val consented = result.getStepResult(FoundationTaskProvider.CONSENT_DOC).result as Boolean

        if (consented) {
            StorageAccess.getInstance().appDatabase.saveTaskResult(result)

            val prefs = AppPrefs.getInstance(this)
            prefs.setHasConsented(true)

            initViews()
        }
    }

    private fun printConsentInfo(consentedDate: TextView, consentedSig: ImageView) {
        val result = StorageAccess.getInstance()
                .appDatabase
                .loadLatestTaskResult(FoundationTaskProvider.CONSENT)

        val signatureBase64 = result.getStepResult(FoundationTaskProvider.SIGNATURE)
                .getResultForIdentifier(ConsentSignatureStepLayout.KEY_SIGNATURE) as String

        val signatureDate = result.getStepResult(FoundationTaskProvider.SIGNATURE)
                .getResultForIdentifier(ConsentSignatureStepLayout.KEY_SIGNATURE_DATE) as String

        consentedDate.text = signatureDate

        val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
        consentedSig.setImageBitmap(BitmapFactory.decodeByteArray(
                signatureBytes,
                0,
                signatureBytes.size))
    }


    // Survey Stuff

    private fun launchSurvey() {
//        val instructionStep = InstructionStep(INSTRUCTION,
//                "Selection Survey",
//                "This survey can help us understand your eligibility for the fitness study")
//        instructionStep.stepTitle = R.string.survey
//
//        val format = TextAnswerFormat()
//        val ageStep = QuestionStep(NAME, "What is your name?", format)
//        ageStep.stepTitle = R.string.survey
//
//        val dateFormat = DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date)
//        val dateStep = QuestionStep(DATE, "Enter a date", dateFormat)
//        dateStep.stepTitle = R.string.survey
//
//        // Create a Boolean step to include in the task.
//        val booleanStep = QuestionStep(NUTRITION)
//        booleanStep.stepTitle = R.string.survey
//        booleanStep.title = "Do you take nutritional supplements?"
//        booleanStep.answerFormat = BooleanAnswerFormat(getString(R.string.rsf_yes),
//                getString(R.string.rsf_no))
//        booleanStep.isOptional = false
//
//        val multiStep = QuestionStep(MULTI_STEP)
//        multiStep.stepTitle = R.string.survey
//        val multiFormat = ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
//                Choice("Zero", 0),
//                Choice("One", 1),
//                Choice("Two", 2))
//        multiStep.title = "Select multiple"
//        multiStep.answerFormat = multiFormat
//        multiStep.isOptional = false
//
//        // Create a task wrapping the steps.
//        val task = OrderedTask(SAMPLE_SURVEY, instructionStep, ageStep, dateStep,
//                // formStep,
//                booleanStep, multiStep)

        val task = this.taskProvider.task(FoundationTaskProvider.SAMPLE_SURVEY)!! as Task

        // Create an activity using the task and set a delegate.
        val intent = ViewTaskActivity.newIntent(this, task)
        startActivityForResult(intent, REQUEST_SURVEY)
    }

//    private fun createFormStep(): FormStep {
//        val formStep = FormStep(FORM_STEP, "Form", "Form groups multi-entry in one page")
//        val formItems = ArrayList<QuestionStep>()
//
//        val basicInfoHeader = QuestionStep(BASIC_INFO_HEADER,
//                "Basic Information",
//                UnknownAnswerFormat())
//        formItems.add(basicInfoHeader)
//
//        val format = TextAnswerFormat()
//        format.setIsMultipleLines(false)
//        val nameItem = QuestionStep(FORM_NAME, "Name", format)
//        formItems.add(nameItem)
//
//        val ageItem = QuestionStep(FORM_AGE, "Age", IntegerAnswerFormat(18, 90))
//        formItems.add(ageItem)
//
//        val genderFormat = ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.SingleChoice,
//                Choice("Male", 0),
//                Choice("Female", 1))
//        val genderFormItem = QuestionStep(FORM_GENDER, "Gender", genderFormat)
//        formItems.add(genderFormItem)
//
//        val multiFormat = ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
//                Choice("Zero", 0),
//                Choice("One", 1),
//                Choice("Two", 2))
//        val multiFormItem = QuestionStep(FORM_MULTI_CHOICE, "Test Multi", multiFormat)
//        formItems.add(multiFormItem)
//
//        val dateOfBirthFormat = DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date)
//        val dateOfBirthFormItem = QuestionStep(FORM_DATE_OF_BIRTH,
//                "Birthdate",
//                dateOfBirthFormat)
//        formItems.add(dateOfBirthFormItem)
//
//        // ... And so on, adding additional items
//        formStep.formSteps = formItems
//        return formStep
//    }

    private fun processSurveyResult(result: TaskResult) {
        StorageAccess.getInstance().appDatabase.saveTaskResult(result)

        val prefs = AppPrefs.getInstance(this)
        prefs.setHasSurveyed(true)
        initViews()
    }

    private fun printSurveyInfo(surveyAnswer: TextView) {
        val taskResult = StorageAccess.getInstance()
                .appDatabase
                .loadLatestTaskResult(FoundationTaskProvider.SAMPLE_SURVEY)

        var results = ""
        for (id in taskResult.results.keys) {
            val stepResult = taskResult.getStepResult(id)
            results += id + ": " + stepResult.result.toString() + "\n"
        }

        surveyAnswer.text = results
    }



}
