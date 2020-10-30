module.exports = {
    testMatch: ['**/tests/*.js'],
    testEnvironment: './CustomNodeEnvironment',
    testTimeout: 350000,
    setupFilesAfterEnv: ['./setupTests.js'],
    testRunner: 'jest-circus/runner',
};
