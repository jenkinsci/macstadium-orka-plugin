const NodeEnvironment = require('jest-environment-node');

class CustomNodeEnvironment extends NodeEnvironment {
    constructor(config) {
        super(config);
    }

    getTestName(test) {
        if (!test || !test.name) {
            return '';
        }
        let testName = test.name;
        let parent = test.parent;
        while (parent) {
            if (parent.name === 'ROOT_DESCRIBE_BLOCK') {
                break;
            }
            testName = `${parent.name} >> ${testName}`;
            parent = parent.parent;
        }

        return testName;
    }

    async handleTestEvent(event, state) {
        const testName = this.getTestName(event.test);
        switch (event.name) {
            case 'test_start':
                console.log(`\x1b[45mSTARTED\x1b[0m ${testName}`);
                break;
            case 'test_done':
                if (event.test.errors.length > 0) {
                    console.log(`\x1b[41mFAILED\x1b[0m ${testName}`);

                    for (const e of event.test.errors) {
                        console.log(e[0].stack);
                    }
                } else {
                    console.log(`\x1b[42mPASSED\x1b[0m ${testName}`);
                }
                break;
        }
    }
}

module.exports = CustomNodeEnvironment;
