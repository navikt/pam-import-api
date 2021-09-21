class ApiError extends Error {
    constructor(message, code) {
        super();
        this.message = message;
        this.code = code;
    }
}

const get = async (url) => new Promise((resolve, reject) => {
    console.log(url)
    fetch(url, { method: 'GET', mode: 'cors' }).then((result) => {
        if (result.status === 200) {
            return resolve(result.json());
        }
        return reject(
            new ApiError(`Error while fetching: ${url}`, result.status)
        );
    });
});


export const getStillingByUuid = (uuid) => get(`${__API__}/${uuid}`);

export const getStillingByProviderIdReference = (providerId,reference) => get(`${__API__}/${providerId}/${reference}`);
