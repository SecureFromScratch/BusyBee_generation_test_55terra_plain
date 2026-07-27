plugins {
    application
}

group = "org.owasp.untrust"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.owasp.untrust.tesseractmock.TesseractMock"
}
