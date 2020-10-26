const {
    createToken: createTokenApi,
    createUser: createUserApi,
    deleteUser: deleteUserApi,
} = require('./orkaApiRoutes');

const { callApi } = require('./utils');
const { LICENSE_KEY } = require('./config');

class UserContext {
    constructor(email, password) {
        this.accessToken = null;
        this.email = email;
        this.password = password;
    }

    async createUser() {
        await callApi([this.email, this.password], createUserApi, {
            licenseKey: LICENSE_KEY,
            shouldCheckErrors: false,
            errorMessage: `User with email ${this.email} cannot be created.`,
        });
    }

    async deleteUser() {
        await callApi([this.email], deleteUserApi, {
            licenseKey: LICENSE_KEY,
            token: this.accessToken,
        });
    }

    async createUserToken() {
        const { token } = await callApi(
            [this.email, this.password],
            createTokenApi,
        );

        this.accessToken = token;
    }

    getToken() {
        return this.accessToken;
    }
}

module.exports = { UserContext };
