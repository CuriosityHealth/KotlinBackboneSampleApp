package org.researchstack.kotlinbackbonesampleapp

import android.content.Context
import org.researchstack.backbone.answerformat.*
import org.researchstack.backbone.interfaces.ITask
import org.researchstack.backbone.interfaces.ITaskProvider
import org.researchstack.backbone.model.Choice
import org.researchstack.backbone.model.ConsentDocument
import org.researchstack.backbone.model.ConsentSection
import org.researchstack.backbone.model.ConsentSignature
import org.researchstack.backbone.step.*
import org.researchstack.backbone.task.OrderedTask
import org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout

class TaskProvider(context: Context): ITaskProvider {

    companion object {

        // Task/Step Identifiers
        val FORM_STEP = "form_step"
        val AGE = "age"
        val INSTRUCTION = "identifier"
        val BASIC_INFO_HEADER = "basic_info_header"
        val FORM_AGE = "form_age"
        val FORM_GENDER = "gender"
        val FORM_MULTI_CHOICE = "multi_choice"
        val FORM_DATE_OF_BIRTH = "date_of_birth"
        val NUTRITION = "nutrition"
        val SIGNATURE = "signature"
        val SIGNATURE_DATE = "signature_date"
        val VISUAL_CONSENT_IDENTIFIER = "visual_consent_identifier"
        val CONSENT_DOC = "consent_doc"
        val SIGNATURE_FORM_STEP = "form_step"
        val NAME = "name"
        val CONSENT = "consent"
        val MULTI_STEP = "multi_step"
        val DATE = "date"
        val DECIMAL = "decimal"
        val FORM_NAME = "form_name"
        val SAMPLE_SURVEY = "sample_survey"


        fun consentTask(context: Context): ITask {

            val document = ConsentDocument()
            document.setTitle("Demo Consent")
            document.signaturePageTitle = R.string.rsb_consent

            // Create consent visual sections
            val section1 = ConsentSection(ConsentSection.Type.DataGathering)
            section1.title = "The title of the section goes here ..."
            section1.summary = "The summary about the section goes here ..."
            section1.content = "The content to show in learn more ..."

            // ...add more sections as needed, then create a visual consent step
            val visualStep = ConsentVisualStep(TaskProvider.VISUAL_CONSENT_IDENTIFIER)
            visualStep.stepTitle = R.string.rsb_consent
            visualStep.section = section1
            visualStep.nextButtonString = context.getString(R.string.rsb_next)

            // Create consent signature object and set what info is required
            val signature = ConsentSignature()
            signature.setRequiresName(true)
            signature.setRequiresSignatureImage(true)

            // Create our HTML to show the user and have them accept or decline.
            val docBuilder = StringBuilder(
                    "</br><div style=\"padding: 10px 10px 10px 10px;\" class='header'>")
            val title = context.getString(R.string.rsb_consent_review_title)
            docBuilder.append(String.format(
                    "<h1 style=\"text-align: center; font-family:sans-serif-light;\">%1\$s</h1>",
                    title))
            val detail = context.getString(R.string.rsb_consent_review_instruction)
            docBuilder.append(String.format("<p style=\"text-align: center\">%1\$s</p>", detail))
            docBuilder.append("</div></br>")
            docBuilder.append("<div><h2> HTML Consent Doc goes here </h2></div>")

            // Create the Consent doc step, pass in our HTML doc
            val documentStep = ConsentDocumentStep(TaskProvider.CONSENT_DOC)
            documentStep.consentHTML = docBuilder.toString()
            documentStep.confirmMessage = context.getString(R.string.rsb_consent_review_reason)

            // Create Consent form step, to get users first & last name
            val formStep = FormStep(TaskProvider.SIGNATURE_FORM_STEP,
                    "Form Title",
                    "Form step description")
            formStep.stepTitle = R.string.rsb_consent

            val format = TextAnswerFormat()
            format.setIsMultipleLines(false)

            val fullName = QuestionStep(TaskProvider.NAME, "Full name", format)
            formStep.formSteps = listOf(fullName)

            // Create Consent signature step, user can sign their name
            val signatureStep = ConsentSignatureStep(TaskProvider.SIGNATURE)
            signatureStep.stepTitle = R.string.rsb_consent
            signatureStep.title = context.getString(R.string.rsb_consent_signature_title)
            signatureStep.text = context.getString(R.string.rsb_consent_signature_instruction)
            signatureStep.signatureDateFormat = signature.signatureDateFormatString
            signatureStep.isOptional = false
            signatureStep.stepLayoutClass = ConsentSignatureStepLayout::class.java

            // Finally, create and present a task including these steps.
            val consentTask = OrderedTask(TaskProvider.CONSENT,
                    visualStep,
                    documentStep,
                    formStep,
                    signatureStep)

            return consentTask
        }

        fun surveyTask(context: Context): ITask {
            val instructionStep = InstructionStep(TaskProvider.INSTRUCTION,
                    "Selection Survey",
                    "This survey can help us understand your eligibility for the fitness study")
            instructionStep.stepTitle = R.string.survey

            val format = TextAnswerFormat()
            val ageStep = QuestionStep(TaskProvider.NAME, "What is your name?", format)
            ageStep.stepTitle = R.string.survey

            val dateFormat = DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date)
            val dateStep = QuestionStep(TaskProvider.DATE, "Enter a date", dateFormat)
            dateStep.stepTitle = R.string.survey

            // Create a Boolean step to include in the task.
            val booleanStep = QuestionStep(TaskProvider.NUTRITION)
            booleanStep.stepTitle = R.string.survey
            booleanStep.title = "Do you take nutritional supplements?"

            booleanStep.answerFormat = BooleanAnswerFormat(context.getString(R.string.rsb_yes),
                    context.getString(R.string.rsb_no))
            booleanStep.isOptional = false

            val multiStep = QuestionStep(TaskProvider.MULTI_STEP)
            multiStep.stepTitle = R.string.survey
            val multiFormat = ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                    Choice("Zero", 0),
                    Choice("One", 1),
                    Choice("Two", 2))
            multiStep.title = "Select multiple"
            multiStep.answerFormat = multiFormat
            multiStep.isOptional = false

            // Create a task wrapping the steps.
            val task = OrderedTask(TaskProvider.SAMPLE_SURVEY, instructionStep, ageStep, dateStep,
                    // formStep,
                    booleanStep, multiStep)

            return task
        }

        fun generateTaskMap(context: Context): Map<String, ITask> {

            val surveyTask = this.surveyTask(context)
            val consentTask = this.consentTask(context)
            return mapOf(
                    Pair(surveyTask.identifier, surveyTask),
                    Pair(consentTask.identifier, consentTask)
            )
        }

    }

    var tasks: Map<String, ITask> = TaskProvider.generateTaskMap(context)
    override fun task(identifier: String): ITask? {
        return tasks.get(identifier)
    }
}