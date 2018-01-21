package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

class DefaultViolationsEvaluator implements ViolationsEvaluator {

    private final ReportUrlRenderer reportUrlRenderer
    private final Logger logger

    DefaultViolationsEvaluator(ReportUrlRenderer reportUrlRenderer, Logger logger) {
        this.reportUrlRenderer = reportUrlRenderer
        this.logger = logger
    }

    @Override
    void evaluate(PenaltyExtension penaltyExtension, Violations... allViolations) {
        Map<String, Integer> total = [errors: 0, warnings: 0]
        String fullMessage = '\n'
        allViolations.each { Violations violations ->
            int errors = violations.errors
            int warnings = violations.warnings
            if (errors > 0 || warnings > 0) {
                fullMessage += "> ${getViolationsMessage(violations, reportUrlRenderer)}\n"
                total['errors'] += errors
                total['warnings'] += warnings
            }
        }
        int errorsDiff = Math.max(0, total['errors'] - penaltyExtension.maxErrors)
        int warningsDiff = Math.max(0, total['warnings'] - penaltyExtension.maxWarnings)
        if (errorsDiff > 0 || warningsDiff > 0) {
            throw new GradleException("Violations limit exceeded by $errorsDiff errors, $warningsDiff warnings.\n$fullMessage")
        } else {
            logger.warn fullMessage
        }
    }

    private static String getViolationsMessage(Violations violations, ReportUrlRenderer reportUrlRenderer) {
        "$violations.name violations found ($violations.errors errors, $violations.warnings warnings). See the reports at:\n" +
                "${violations.reports.collect { "- ${reportUrlRenderer.render(it)}" }.join('\n')}"
    }
}
