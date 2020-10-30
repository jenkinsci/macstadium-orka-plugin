module.exports = {
    testMatch: ['**/tests/*.js'],
    testEnvironment: './customNodeEnvironment',
    testTimeout: 350000,
    setupFilesAfterEnv: ['./setupTests.js'],
    testRunner: 'jest-circus/runner',
};
