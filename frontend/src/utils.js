const ISO_8601_DATE = /^\d{4}(-\d\d(-\d\d(T\d\d:\d\d(:\d\d)?(\.\d+)?(([+-]\d\d:\d\d)|Z)?)?)?)?$/i;

export function isValidISOString(isoString) {
    return ISO_8601_DATE.test(isoString);
}

export function formatISOString(isoString, format = 'DD.MM.YYYY') {
    try {
        if (isValidISOString(isoString)) {
            const dt = isoString.split('-');
            if (format === 'DD.MM.YYYY') {
                const day = dt[2].split('T')[0];
                return `${day}.${dt[1]}.${dt[0]}`;
            }
            return isoString;
        }
    } catch (error) {
        return isoString;
    }
    return isoString;
}

export function userAgentIsInternetExplorer() {
    const userAgent = window.navigator.userAgent;
    return userAgent.indexOf('MSIE ') >= 0 || userAgent.indexOf('Trident/') >= 0;
}

export function isValidUrl(input) {
    if (userAgentIsInternetExplorer()) {
        // Gracefully degrade, 'new URL(..)' is unsupported in IE
        return false;
    }

    try {
        return new URL(input).protocol.startsWith('http');
    } catch (e) {
        return false;
    }
}

export function isValidEmail(input) {
    // Regex from https://emailregex.com
    const pattern = new RegExp(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/);

    return pattern.test(input);
}

export function containsEmail(input) {
    // Regex from https://emailregex.com
    const preprocessedInput = input.replace(/&#64;/, "@");
    const pattern = new RegExp(/(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))/);

    return pattern.test(preprocessedInput);
}

export function extractEmail(input) {
    // Regex from https://emailregex.com
    const preprocessedInput = input.replace(/&#64;/g, "@");
    const pattern = new RegExp(/([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)/gi);

    return preprocessedInput.match(pattern);
}

export function mailtoInString(input, email) {
    const pattern = new RegExp(`mailto:${email}`, 'g')
    
    return pattern.test(input);
}


export function removeUndefinedOrEmptyString(obj) {
    const newObj = {};
    Object.keys(obj).forEach((prop) => {
        if (obj[prop] !== undefined && obj[prop] !== '') {
            newObj[prop] = obj[prop];
        }
    });
    return newObj;
}

export const isMobile = () => window.matchMedia('(max-width: 991px)').matches;

/**
 * Tar en query-string og returnerer en Object-representasjon av stringen.
 * @param queryString   Query-stringen som skal parseres til et objekt.
 * @returns {Object}    En Object-representasjon av 'queryString'.
 */
export function parseQueryString(queryString = '?') {
    const parameters = queryString.substring(1).split('&');
    const object = {};
    parameters.forEach((parameter) => {
        const pair = parameter.split('=');
        if (pair[0] !== undefined && pair[0] !== '') {
            let key = decodeURIComponent(pair[0]);
            const val = pair[1] !== undefined ? decodeURIComponent(pair[1]) : '';

            if (key === 'international') {
                object[key] = val === 'true' ? true : 'false';
            } else if (key.includes('[]')) {
                key = key.replace('[]', '');

                if (object[key] === undefined) {
                    object[key] = [val];
                } else {
                    object[key].push(val);
                }
            } else {
                object[key] = val;
            }
        }
    });

    return object;
}

function arrayToQueryString(key, array) {
    return array.map((val) => `${encodeURIComponent(key)}[]=${encodeURIComponent(val)}`)
        .join('&');
}

/**
 * Tar et objekt og returnerer en query-string-representasjon av objektet.
 * @param object        Objekt som skal gjøres om til en query-string.
 * @returns {string}    En query-string-representasjon av 'object'.
 */
export function stringifyQueryObject(object = {}) {
    const string = Object.keys(object)
        .map((key) => {
            const value = object[key];
            if (Array.isArray(value)) {
                return arrayToQueryString(key, value);
            }
            return `${encodeURIComponent(key)}=${encodeURIComponent(object[key])}`;
        })
        .filter((elem) => elem !== '')
        .join('&');

    if (string.length > 0) {
        return `?${string}`;
    }
    return ''
}

/**
 * Dekoder en url til den ikke lenger er kodet.
 * En kodet url er i dette tilfellet en url som inneholder '%'.
 */
export function decodeUrl(url) {
    let decodedUrl = url;
    while (decodedUrl.includes('%')) {
        decodedUrl = decodeURIComponent(decodedUrl);
    }
    return decodedUrl;
}
