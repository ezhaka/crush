job("Qodana") {
    container("jetbrains/qodana-jvm:latest") {
        env["QODANA_TOKEN"] = Secrets("qodana-token")
        shellScript {
            content = """
               qodana \
               --fail-threshold 0 \
               --profile-name qodana.starter
               """.trimIndent()
        }
    }
}