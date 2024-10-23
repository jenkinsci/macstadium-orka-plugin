const envalid = require('envalid');

const { str, bool, url, email } = envalid;

const env = envalid.cleanEnv(process.env, {
    JENKINS_URL: str({ default: 'http://localhost:8080/jenkins' }),
    JENKINS_API_URL: url({ default: 'http://admin:admin@localhost:8080' }),
    RUN_HEADLESS: bool({ default: true }),
    API_URL: url({ default: 'http://10.221.188.100' }),
    ORKA_TEST_USER: email({ default: 'it-jenkins@test.com' }),
    ORKA_TEST_USER_PASSWORD: str({ default: '123456' }),
    VM_BASE_IMAGE: str({ default: 'jenkins.img' }),
    LICENSE_KEY: str({ default: 'orka-license-key' }),
});

module.exports = {
    JENKINS_URL: env.JENKINS_URL,
    JENKINS_API_URL: env.JENKINS_API_URL,
    RUN_HEADLESS: env.RUN_HEADLESS,
    API_URL: env.API_URL,
    ORKA_TEST_USER: env.ORKA_TEST_USER,
    ORKA_TEST_USER_PASSWORD: env.ORKA_TEST_USER_PASSWORD,
    VM_BASE_IMAGE: env.VM_BASE_IMAGE,
    LICENSE_KEY: env.LICENSE_KEY,
    TEST_VM_CONFIG_NAME: 'it-orka-jenkins',
};
