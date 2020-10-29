const waitForExpect = require('wait-for-expect');

waitForExpect.defaults.timeout = 100 * 5000;
waitForExpect.defaults.interval = 5000;

jasmine.getEnv().addReporter({
    specStarted: (result) => {
        console.log(`Starting ${result.fullName}`);
    },
    specDone: (result) => {
        if (result.failedExpectations.length > 0) {
            console.log(`FAILED: ${result.fullName}`);
            for (const fe of result.failedExpectations) {
                console.log(fe.stack);
            }
        } else {
            console.log(`PASSED: ${result.fullName}`);
        }
    },
});
