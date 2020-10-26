const { chromium } = require('playwright');
const waitForExpect = require('wait-for-expect');

const { get } = require('../jenkinsApiClient');
const { OrkaApiHelper } = require('../orkaApiHelper');
const {
    JENKINS_URL,
    RUN_HEADLESS,
    ORKA_TEST_USER,
    ORKA_TEST_USER_PASSWORD,
} = require('../config');
const { UserContext } = require('../userContext');

const defaultIdleTime = 30;
const testJobName = 'orka-it-short-job';
const testLongJobName = 'orka-it-long-job';
const idleTimeCloudRetentionStrategy =
    'io.jenkins.plugins.orka.IdleTimeCloudRetentionStrategy';
const runOnceCloudRetentionStrategy =
    'io.jenkins.plugins.orka.RunOnceCloudRetentionStrategy';

describe('Jenkins Plugin - Cloud', () => {
    let browser;
    let page;
    let userContext;
    let orkaApiHelper;

    const getJobBuildAgent = async (
        jobName,
        buildNumber,
        { shouldWaitForJobToFinish = true } = {},
    ) => {
        let lastBuildAgent = null;
        await waitForExpect(async () => {
            const { data } = await get(
                `/job/${jobName}/${buildNumber}/api/json`,
            );

            expect(data).toBeDefined();
            expect(data.id).toBeDefined();
            expect(data.builtOn).toBeDefined();
            if (shouldWaitForJobToFinish) {
                expect(data.building).toBe(false);
                expect(data.result).toEqual('SUCCESS');
            }

            lastBuildAgent = data.builtOn;
        });

        return lastBuildAgent;
    };

    const getNextBuildNumber = async (jobName) => {
        const { data } = await get(`/job/${jobName}/api/json`);

        return data.nextBuildNumber;
    };

    const buildJob = async (
        jobName,
        { shouldWaitForJobToFinish = true } = {},
    ) => {
        const nextBuildNumber = await getNextBuildNumber(jobName);

        await page.goto(`${JENKINS_URL}/job/${jobName}/`);

        await page.click(
            '#tasks >> .task-link-wrapper >> a[title="Build Now"]',
        );

        const buildAgent = await getJobBuildAgent(jobName, nextBuildNumber, {
            shouldWaitForJobToFinish,
        });

        return { buildNumber: nextBuildNumber, buildAgent };
    };

    const getBuildAgents = async () => {
        const { data } = await get('/computer/api/json?computer[displayName]');
        const buildAgents = data.computer.map((a) => a.displayName);
        return buildAgents;
    };

    const deleteAgent = async (buildAgent) => {
        await page.goto(`${JENKINS_URL}/computer/${buildAgent}/delete`);
        await page.click('span[name="Submit"] >> button');
    };

    const cleanUpBuildAgents = async () => {
        const buildAgents = await getBuildAgents();
        for (const ba of buildAgents) {
            if (ba !== 'master') {
                await deleteAgent(ba);
            }
        }
    };

    const verifyAgentConfig = async (
        buildAgent,
        idleTime,
        retentionStrategyValue,
    ) => {
        await page.goto(`${JENKINS_URL}/computer/${buildAgent}/configure`);

        const numberOfExecutors = await page.$eval(
            'input[name="_.numExecutors"]',
            (el) => el.value,
        );
        expect(numberOfExecutors).toBe('1');

        const remoteFs = await page.$eval(
            'input[name="_.remoteFS"]',
            (el) => el.value,
        );
        expect(remoteFs).toBe('/Users/admin');

        const usage = await page.$eval('select[name="mode"]', (el) => el.value);
        expect(usage).toBe('NORMAL');

        const retentionStrategy = await page.$eval(
            '.dropdownList',
            (el) => el.value,
        );

        expect(retentionStrategy).toBe(retentionStrategyValue);
        const retentionStrategyIdleTimeInMinutes = await page.$eval(
            'input[name="_.idleMinutes"]',
            (el) => el.value,
        );

        expect(retentionStrategyIdleTimeInMinutes).toBe(idleTime.toString());
    };

    const waitForVmConfigToLoad = async () => {
        const vmConfigs = await page.$('select[name="_.vm"]');
        await vmConfigs.waitForElementState('stable');
    };

    const changeCloudConfigIdleTime = async (newIdleTime) => {
        await page.goto(`${JENKINS_URL}/configureClouds/`);
        await waitForVmConfigToLoad();
        await page.fill('input[name="_.idleMinutes"]', newIdleTime.toString());
        await page.click('span[name="Apply"] >> button');
        await page.click('span[name="Submit"] >> button');
    };

    const verifyAgentVmIsRemoved = async (agentName) => {
        const vms = await orkaApiHelper.getAllVms();

        for (const vm of vms) {
            if (vm.status) {
                expect(vm.status.virtual_machine_id).not.toEqual(agentName);
            }
        }
    };

    const changeRetentionStrategy = async (retentionStrategy) => {
        await page.goto(`${JENKINS_URL}/configureClouds/`);

        await waitForVmConfigToLoad();

        const retentionStrategyDropdown = await page.$('css=.dropdownList');
        const optionValue = await retentionStrategyDropdown.$eval(
            'option',
            (o) => o.value,
        );

        // ensure the right dropdown is selected
        expect([
            runOnceCloudRetentionStrategy,
            idleTimeCloudRetentionStrategy,
        ]).toContain(optionValue);

        retentionStrategyDropdown.selectOption({ value: retentionStrategy });

        await page.click('span[name="Apply"] >> button');
        await page.click('span[name="Submit"] >> button');
    };

    beforeAll(async () => {
        userContext = new UserContext(ORKA_TEST_USER, ORKA_TEST_USER_PASSWORD);
        await userContext.createUser();
        await userContext.createUserToken();

        orkaApiHelper = new OrkaApiHelper(userContext.getToken());
        await orkaApiHelper.createTestVmConfig();

        browser = await chromium.launch({
            headless: RUN_HEADLESS,
        });
        const context = await browser.newContext();
        page = await context.newPage();
        await page.goto(`${JENKINS_URL}`);
    });

    afterAll(async () => {
        if (browser) {
            browser.close();
        }

        await orkaApiHelper.purgeTestVm();
        await userContext.deleteUser();
    });

    afterEach(async () => {
        await cleanUpBuildAgents();
    });

    describe('with Retention Strategy "Keep until idle time expires"', () => {
        it('should load correct agent settings', async () => {
            const { buildAgent } = await buildJob(testJobName);
            const buildAgentsList = await getBuildAgents();
            expect(buildAgentsList).toHaveLength(2); // agents list must contain `master` and the agent created by the job build
            const isLastBuildAgentInList = buildAgentsList.includes(buildAgent);
            expect(isLastBuildAgentInList).toBe(true);

            await verifyAgentConfig(
                buildAgent,
                defaultIdleTime,
                idleTimeCloudRetentionStrategy,
            );
        });

        it('should pick the same agent on second build', async () => {
            const { buildAgent: jobBuildAgent1 } = await buildJob(testJobName);
            const { buildAgent: jobBuildAgent2 } = await buildJob(testJobName);
            expect(jobBuildAgent1).toEqual(jobBuildAgent2);
        });

        describe('and idle time changed', () => {
            afterEach(async () => {
                await changeCloudConfigIdleTime(defaultIdleTime);
            });

            it('should load correct agent settings after cloud config is changed', async () => {
                const newIdleTime = 20;

                await changeCloudConfigIdleTime(newIdleTime);
                const { buildAgent } = await buildJob(testJobName);
                await verifyAgentConfig(
                    buildAgent,
                    newIdleTime,
                    idleTimeCloudRetentionStrategy,
                );
                await deleteAgent(buildAgent);
            });

            it('should delete agent and vm when idle time expires', async () => {
                const shortIdleTime = 1;
                await changeCloudConfigIdleTime(shortIdleTime);
                const { buildAgent } = await buildJob(testJobName);
                await verifyAgentConfig(
                    buildAgent,
                    shortIdleTime,
                    idleTimeCloudRetentionStrategy,
                );
                await waitForExpect(async () => {
                    const buildAgentsList = await getBuildAgents();
                    expect(buildAgentsList).toHaveLength(1);
                    expect(buildAgentsList[0]).toEqual('master');
                });
                await verifyAgentVmIsRemoved(buildAgent);
            });
        });
    });

    describe('with Retention Strategy "Terminate immediately after use"', () => {
        beforeAll(async () => {
            await changeRetentionStrategy(runOnceCloudRetentionStrategy);
        });

        afterAll(async () => {
            await changeRetentionStrategy(idleTimeCloudRetentionStrategy);
        });

        it('should load correct agent settings', async () => {
            const { buildAgent } = await buildJob(testLongJobName, {
                shouldWaitForJobToFinish: false,
            });

            await verifyAgentConfig(
                buildAgent,
                defaultIdleTime,
                runOnceCloudRetentionStrategy,
            );
        });

        it('should delete agent and vm when job finishes', async () => {
            const { buildAgent, buildNumber } = await buildJob(testJobName);

            await waitForExpect(async () => {
                const { data } = await get(
                    `/job/${testJobName}/${buildNumber}/api/json`,
                );

                expect(data.result).toEqual('SUCCESS');
            });

            await waitForExpect(async () => {
                const buildAgents = await getBuildAgents();

                expect(buildAgents).toHaveLength(1);
                expect(buildAgents[0]).toEqual('master');
            });

            await verifyAgentVmIsRemoved(buildAgent);
        });
    });
});
