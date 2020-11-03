const { get, post, deleteAction } = require('./orkaApiClient');

const createToken = (email, password) => post('/token', { email, password });
const revokeToken = ({ token }) => deleteAction('/token', null, { token });

const listVms = ({ token }) => get('/resources/vm/list', {}, { token });

const createUser = (email, password, { licenseKey }) =>
    post('/users', { email, password }, { licenseKey });
const deleteUser = (username, { licenseKey, token }) =>
    deleteAction(`/users/${username}`, null, { token, licenseKey });

const createVmConfig = (data, { token }) =>
    post('/resources/vm/create', data, { token });
const purgeVm = (data, { token }) =>
    deleteAction('/resources/vm/purge', data, { token });

module.exports = {
    createToken,
    revokeToken,
    listVms,
    createUser,
    deleteUser,
    createVmConfig,
    purgeVm,
};
