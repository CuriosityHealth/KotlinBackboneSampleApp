package org.researchstack.kotlinbackbonesampleapp

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.researchstack.backbone.StorageAccess
import org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout

class MainFragment: Fragment() {

    // Views
    private var container: View? = null
    private var consentButton: AppCompatButton? = null
    private var surveyButton: AppCompatButton? = null

    public var launchConsentHandler: (()->Unit)? = null
    public var launchSurveyHandler: (()->Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        this.container = view

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val appCompatActivity: AppCompatActivity = this.activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(toolbar)
        appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        this.consentButton = view.findViewById(R.id.consent_button)
        this.consentButton!!.setOnClickListener {
            launchConsentHandler?.let {
                it()
            }
        }

        this.surveyButton = view.findViewById(R.id.survey_button)
        this.surveyButton!!.setOnClickListener {
            launchSurveyHandler?.let {
                it()
            }
        }


        return view
    }

    override fun onResume() {
        super.onResume()

        this.initViews(this.activity as AppCompatActivity)
    }

    public fun initViews(context: Context) {

        val prefs = AppPrefs.getInstance(context)

        val lblConsentedDate = this.container!!.findViewById(R.id.consented_date_lbl) as TextView
        val consentedDate = this.container!!.findViewById(R.id.consented_date) as TextView
        val consentedSig = this.container!!.findViewById(R.id.consented_signature) as ImageView

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

        val surveyAnswer = this.container!!.findViewById(R.id.survey_results) as TextView

        if (prefs.hasSurveyed()) {
            surveyAnswer.visibility = View.VISIBLE
            printSurveyInfo(surveyAnswer)
        } else {
            surveyAnswer.visibility = View.GONE
        }
    }

    private fun printSurveyInfo(surveyAnswer: TextView) {
        val taskResult = StorageAccess.getInstance()
                .appDatabase
                .loadLatestTaskResult(MainActivity.SAMPLE_SURVEY)

        var results = ""
        for (id in taskResult.results.keys) {
            val stepResult = taskResult.getStepResult(id)
            results += id + ": " + stepResult.result.toString() + "\n"
        }

        surveyAnswer.text = results
    }

    private fun printConsentInfo(consentedDate: TextView, consentedSig: ImageView) {
        val result = StorageAccess.getInstance()
                .appDatabase
                .loadLatestTaskResult(MainActivity.CONSENT)

        val signatureBase64 = result.getStepResult(MainActivity.SIGNATURE)
                .getResultForIdentifier(ConsentSignatureStepLayout.KEY_SIGNATURE) as String

        val signatureDate = result.getStepResult(MainActivity.SIGNATURE)
                .getResultForIdentifier(ConsentSignatureStepLayout.KEY_SIGNATURE_DATE) as String

        consentedDate.text = signatureDate

        val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
        consentedSig.setImageBitmap(BitmapFactory.decodeByteArray(
                signatureBytes,
                0,
                signatureBytes.size))
    }

}