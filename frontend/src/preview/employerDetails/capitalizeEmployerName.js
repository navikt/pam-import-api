function capitalizeEmployerName(text) {
    const separators = [' ', '-', '(', '/'];

    const ignore = [
        'i', 'og', 'for', 'på', 'avd', 'av'
    ];

    const keep = [
        'as', 'ab', 'asa', 'ba', 'sa'
    ];

    if (text) {
        let capitalized = text.toLowerCase();

        for (let i = 0, len = separators.length; i < len; i += 1) {
            const fragments = capitalized.split(separators[i]);
            for (let j = 0, x = fragments.length; j < x; j += 1) {
                if (keep.includes(fragments[j])) {
                    fragments[j] = fragments[j].toUpperCase();
                } else if (!ignore.includes(fragments[j])) {
                    if (fragments[j][0] !== undefined) {
                        fragments[j] = fragments[j][0].toUpperCase() + fragments[j].substr(1);
                    }
                }
            }
            capitalized = fragments.join(separators[i]);
        }

        return capitalized;
    }
    return text;
}

module.exports = capitalizeEmployerName;
