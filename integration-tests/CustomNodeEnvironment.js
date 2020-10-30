const NodeEnvironment = require('jest-environment-node');

class CustomNodeEnvironment extends NodeEnvironment {
    constructor(config) {
        super(config);
    }

    async handleTestEvent(event, state) {
        if (event.name === 'test_start') {
            console.log(
                `\x1b[45mSTARTED\x1b[0m ${event.test.parent.name} >> ${event.test.name}`,
            );
        }

        if (event.name === 'test_done') {
            const testName = `${event.test.parent.name} >> ${event.test.name}`;

            if (event.test.errors.length > 0) {
                console.log(`\x1b[41mFAILED\x1b[0m ${testName}`, 'color:red');

                for (const e of event.test.errors) {
                    console.log(e[0].stack);
                }
            } else {
                console.log(
                    `\x1b[42mPASSED\x1b[0m ${testName}`,
                    'color: green',
                );
            }
        }
    }
}

module.exports = CustomNodeEnvironment;
