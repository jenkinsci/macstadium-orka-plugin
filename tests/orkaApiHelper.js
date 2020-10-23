const { listVms, createVmConfig, purgeVm } = require('./orkaApiRoutes');

const { VM_BASE_IMAGE, TEST_VM_CONFIG_NAME } = require('./config');

const { callApi } = require('./utils');

class OrkaApiHelper {
    constructor(token) {
        this.token = token;
    }

    async createTestVmConfig() {
        const requestData = {
            orka_vm_name: TEST_VM_CONFIG_NAME,
            orka_base_image: VM_BASE_IMAGE,
            orka_image: TEST_VM_CONFIG_NAME,
            orka_cpu_core: 3,
            vcpu_count: 3,
        };

        await callApi([requestData], createVmConfig, {
            token: this.token,
            shouldCheckErrors: false,
            errorMessage: `Vm config ${TEST_VM_CONFIG_NAME} cannot be created.`,
        });
    }

    async purgeTestVm() {
        const requestData = {
            orka_vm_name: TEST_VM_CONFIG_NAME,
        };

        await callApi([requestData], purgeVm, {
            token: this.token,
        });
    }

    async getAllVms() {
        const data = await callApi([], listVms, {
            token: this.token,
        });
        return data.virtual_machine_resources;
    }
}

module.exports = {
    OrkaApiHelper,
};
