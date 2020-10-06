const worktimeParser = (worktime) => {
    // We need this check in case of old workhour/-day property values, formatted like 'Opt1 Opt2'
    let arrayString = '';
    try {
        const jsonArray = JSON.parse(worktime);

        for (let i = 0; i < jsonArray.length; i++) {
            arrayString += `${jsonArray[i]} `;
        }
    } catch (e) {
        arrayString = worktime;
    }
    return arrayString;
};

export default worktimeParser;
