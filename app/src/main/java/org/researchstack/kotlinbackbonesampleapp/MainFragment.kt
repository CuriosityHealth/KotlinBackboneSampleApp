//package org.researchstack.kotlinbackbonesampleapp
//
//import android.content.Context
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.support.v4.app.Fragment
//import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.AppCompatButton
//import android.support.v7.widget.Toolbar
//import android.util.Base64
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import org.researchstack.feature.storage.StorageAccess
//import org.researchstack.foundation.components.common.ui.activities.ViewTaskActivity
//import org.researchstack.feature.consent.model.ConsentDocument
//import org.researchstack.feature.consent.model.ConsentSection
//import org.researchstack.feature.consent.model.ConsentSignature
//import org.researchstack.feature.consent.step.ConsentDocumentStep
//import org.researchstack.feature.consent.step.ConsentSignatureStep
//import org.researchstack.feature.consent.step.ConsentVisualStep
//import org.researchstack.feature.consent.ui.layout.ConsentSignatureStepLayout
//import org.researchstack.foundation.components.survey.answerformat.*
//import org.researchstack.foundation.components.survey.model.Choice
//import org.researchstack.foundation.components.survey.step.FormStep
//import org.researchstack.foundation.components.survey.step.InstructionStep
//import org.researchstack.foundation.components.survey.step.QuestionStep
//import org.researchstack.foundation.components.common.task.OrderedTask
//import org.researchstack.foundation.core.models.result.TaskResult
//import java.util.*
//
//open class MainFragment:  Fragment() {
//
//    companion object {
//
//        val TAG = MainFragment.javaClass.name
//        // Activity Request Codes
//        val REQUEST_CONSENT = 0
//        val REQUEST_SURVEY = 1
//        val REQUEST_PIN_CODE = 2
//
//        // Task/Step Identifiers
//        val FORM_STEP = "form_step"
//        val AGE = "age"
//        val INSTRUCTION = "identifier"
//        val BASIC_INFO_HEADER = "basic_info_header"
//        val FORM_AGE = "form_age"
//        val FORM_GENDER = "gender"
//        val FORM_MULTI_CHOICE = "multi_choice"
//        val FORM_DATE_OF_BIRTH = "date_of_birth"
//        val NUTRITION = "nutrition"
//        val SIGNATURE = "signature"
//        val SIGNATURE_DATE = "signature_date"
//        val VISUAL_CONSENT_IDENTIFIER = "visual_consent_identifier"
//        val CONSENT_DOC = "consent_doc"
//        val SIGNATURE_FORM_STEP = "form_step"
//        val NAME = "name"
//        val CONSENT = "consent"
//        val MULTI_STEP = "multi_step"
//        val DATE = "date"
//        val DECIMAL = "decimal"
//        val FORM_NAME = "form_name"
//        val SAMPLE_SURVEY = "sample_survey"
//        val PIN_CODE = "pin_code"
//    }
//
//    // Views
//    private var container: View? = null
//    private var consentButton: AppCompatButton? = null
//    private var surveyButton: AppCompatButton? = null
//
////    override fun onCreate(savedInstanceState: Bundle?) {
//////        super.onCreate(savedInstanceState)
//////        setContentView(R.layout.activity_main)
//////
//////        val toolbar = findViewById(R.id.toolbar) as Toolbar
//////        setSupportActionBar(toolbar)
//////        this.supportActionBar!!.setDisplayShowTitleEnabled(true)
//////
////        this.consentButton = findViewById(R.id.consent_button) as AppCompatButton
////        this.consentButton!!.setOnClickListener {
////            launchConsent()
////        }
////
////        this.surveyButton = findViewById(R.id.survey_button) as AppCompatButton
////        this.surveyButton!!.setOnClickListener {
////            launchSurvey()
////        }
//////
//////    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//
//        this.container = inflater.inflate(R.layout.fragment_main, container, false)
//
//        val toolbar = this.container!!.findViewById(R.id.toolbar) as Toolbar?
//
//        val appCompatActivity: AppCompatActivity = this.activity as AppCompatActivity
//        appCompatActivity.setSupportActionBar(toolbar)
//        appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//
//        this.consentButton = this.container!!.findViewById(R.id.consent_button) as AppCompatButton
//        this.consentButton!!.setOnClickListener {
//            launchConsent()
//        }
//
//        this.surveyButton = this.container!!.findViewById(R.id.survey_button) as AppCompatButton
//        this.surveyButton!!.setOnClickListener {
//            launchSurvey()
//        }
//
//        return view
//    }
//
//
//    private fun initViews() {
//        val prefs = AppPrefs.getInstance(this.activity as Context)
//
//        val lblConsentedDate = this.container!!.findViewById(R.id.consented_date_lbl) as TextView
//        val consentedDate = this.container!!.findViewById(R.id.consented_date) as TextView
//        val consentedSig = this.container!!.findViewById(R.id.consented_signature) as ImageView
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
//        val surveyAnswer = this.container!!.findViewById(R.id.survey_results) as TextView
//
//        if (prefs.hasSurveyed()) {
//            surveyAnswer.visibility = View.VISIBLE
//            printSurveyInfo(surveyAnswer)
//        } else {
//            surveyAnswer.visibility = View.GONE
//        }
//    }
//
//
//    // Consent stuff
//
//    private fun launchConsent() {
//        val document = ConsentDocument()
//        document.setTitle("Demo Consent")
//        document.signaturePageTitle = R.string.rsfc_consent
//
//        // Create consent visual sections
//        val section1 = ConsentSection(ConsentSection.Type.DataGathering)
//        section1.title = "The title of the section goes here ..."
//        section1.summary = "The summary about the section goes here ..."
//        section1.content = "The content to show in learn more ..."
//
//        // ...add more sections as needed, then create a visual consent step
//        val visualStep = ConsentVisualStep(MainActivity.VISUAL_CONSENT_IDENTIFIER)
//        visualStep.stepTitle = R.string.rsfc_consent
//        visualStep.section = section1
//        visualStep.nextButtonString = getString(R.string.rsf_next)
//
//        // Create consent signature object and set what info is required
//        val signature = ConsentSignature()
//        signature.setRequiresName(true)
//        signature.setRequiresSignatureImage(true)
//
//        // Create our HTML to show the user and have them accept or decline.
//        val docBuilder = StringBuilder(
//                "</br><div style=\"padding: 10px 10px 10px 10px;\" class='header'>")
//        val title = getString(R.string.rsfc_consent_review_title)
//        docBuilder.append(String.format(
//                "<h1 style=\"text-align: center; font-family:sans-serif-light;\">%1\$s</h1>",
//                title))
//        val detail = getString(R.string.rsfc_consent_review_instruction)
//        docBuilder.append(String.format("<p style=\"text-align: center\">%1\$s</p>", detail))
//        docBuilder.append("</div></br>")
//        docBuilder.append("<div><h2> HTML Consent Doc goes here </h2></div>")
//
//        // Create the Consent doc step, pass in our HTML doc
//        val documentStep = ConsentDocumentStep(MainActivity.CONSENT_DOC)
//        documentStep.consentHTML = docBuilder.toString()
//        documentStep.confirmMessage = getString(R.string.rsfc_consent_review_reason)
//
//        // Create Consent form step, to get users first & last name
//        val formStep = FormStep(MainActivity.SIGNATURE_FORM_STEP,
//                "Form Title",
//                "Form step description")
//        formStep.stepTitle = R.string.rsfc_consent
//
//        val format = TextAnswerFormat()
//        format.setIsMultipleLines(false)
//
//        val fullName = QuestionStep(MainActivity.NAME, "Full name", format)
//        formStep.formSteps = listOf(fullName)
//
//        // Create Consent signature step, user can sign their name
//        val signatureStep = ConsentSignatureStep(MainActivity.SIGNATURE)
//        signatureStep.stepTitle = R.string.rsfc_consent
//        signatureStep.title = getString(R.string.rsfc_consent_signature_title)
//        signatureStep.text = getString(R.string.rsfc_consent_signature_instruction)
//        signatureStep.signatureDateFormat = signature.signatureDateFormatString
//        signatureStep.isOptional = false
//        signatureStep.setStepLayoutClass(ConsentSignatureStepLayout::class.java)
//
//        // Finally, create and present a task including these steps.
//        val consentTask = OrderedTask(MainActivity.CONSENT,
//                visualStep,
//                documentStep,
//                formStep,
//                signatureStep)
//
//        // Launch using hte ViewTaskActivity and make sure to listen for the activity result
//        val intent = ViewTaskActivity.newIntent(this.activity as Context, consentTask)
//        startActivityForResult(intent, MainActivity.REQUEST_CONSENT)
//    }
//
//    private fun processConsentResult(result: TaskResult) {
//        val consented = result.getStepResult(MainActivity.CONSENT_DOC).result as Boolean
//
//        if (consented) {
//            StorageAccess.getInstance().appDatabase.saveTaskResult(result)
//
//            val prefs = AppPrefs.getInstance(this.activity as Context)
//            prefs.setHasConsented(true)
//
//            initViews()
//        }
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
//
//    // Survey Stuff
//
//    private fun launchSurvey() {
//        val instructionStep = InstructionStep(MainActivity.INSTRUCTION,
//                "Selection Survey",
//                "This survey can help us understand your eligibility for the fitness study")
//        instructionStep.stepTitle = R.string.survey
//
//        val format = TextAnswerFormat()
//        val ageStep = QuestionStep(MainActivity.NAME, "What is your name?", format)
//        ageStep.stepTitle = R.string.survey
//
//        val dateFormat = DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date)
//        val dateStep = QuestionStep(MainActivity.DATE, "Enter a date", dateFormat)
//        dateStep.stepTitle = R.string.survey
//
//        // Create a Boolean step to include in the task.
//        val booleanStep = QuestionStep(MainActivity.NUTRITION)
//        booleanStep.stepTitle = R.string.survey
//        booleanStep.title = "Do you take nutritional supplements?"
//        booleanStep.answerFormat = BooleanAnswerFormat(getString(R.string.rsf_yes),
//                getString(R.string.rsf_no))
//        booleanStep.isOptional = false
//
//        val multiStep = QuestionStep(MainActivity.MULTI_STEP)
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
//        val task = OrderedTask(MainActivity.SAMPLE_SURVEY, instructionStep, ageStep, dateStep,
//                // formStep,
//                booleanStep, multiStep)
//
//        // Create an activity using the task and set a delegate.
//        val intent = ViewTaskActivity.newIntent(this.activity as Context, task)
//        startActivityForResult(intent, MainActivity.REQUEST_SURVEY)
//    }
//
//    private fun createFormStep(): FormStep {
//        val formStep = FormStep(MainActivity.FORM_STEP, "Form", "Form groups multi-entry in one page")
//        val formItems = ArrayList<QuestionStep>()
//
//        val basicInfoHeader = QuestionStep(MainActivity.BASIC_INFO_HEADER,
//                "Basic Information",
//                UnknownAnswerFormat())
//        formItems.add(basicInfoHeader)
//
//        val format = TextAnswerFormat()
//        format.setIsMultipleLines(false)
//        val nameItem = QuestionStep(MainActivity.FORM_NAME, "Name", format)
//        formItems.add(nameItem)
//
//        val ageItem = QuestionStep(MainActivity.FORM_AGE, "Age", IntegerAnswerFormat(18, 90))
//        formItems.add(ageItem)
//
//        val genderFormat = ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.SingleChoice,
//                Choice("Male", 0),
//                Choice("Female", 1))
//        val genderFormItem = QuestionStep(MainActivity.FORM_GENDER, "Gender", genderFormat)
//        formItems.add(genderFormItem)
//
//        val multiFormat = ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
//                Choice("Zero", 0),
//                Choice("One", 1),
//                Choice("Two", 2))
//        val multiFormItem = QuestionStep(MainActivity.FORM_MULTI_CHOICE, "Test Multi", multiFormat)
//        formItems.add(multiFormItem)
//
//        val dateOfBirthFormat = DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date)
//        val dateOfBirthFormItem = QuestionStep(MainActivity.FORM_DATE_OF_BIRTH,
//                "Birthdate",
//                dateOfBirthFormat)
//        formItems.add(dateOfBirthFormItem)
//
//        // ... And so on, adding additional items
//        formStep.formSteps = formItems
//        return formStep
//    }
//
//    private fun processSurveyResult(result: TaskResult) {
//        StorageAccess.getInstance().appDatabase.saveTaskResult(result)
//
//        val prefs = AppPrefs.getInstance(this.activity as Context)
//        prefs.setHasSurveyed(true)
//        initViews()
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
//
//
//}