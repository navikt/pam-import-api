const capitalizeEmployerName = require('./capitalizeEmployerName');

function getEmployer(stilling) {
    if (stilling && stilling.properties && stilling.properties.employer) {
        return stilling.properties.employer;
    }
    if (stilling && stilling.businessName) {
        return stilling.businessName;
    }
    if (stilling && stilling.employer) {
        return capitalizeEmployerName(stilling.employer.name);
    }

    return null;
}

module.exports = getEmployer;
