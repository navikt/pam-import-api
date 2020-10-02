import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import '../styles.less';
import {getStillingByUuid} from "../api/api";




const Preview = ({ match }) => {
    const [ad, setAd] = useState(undefined);
    useEffect(() => {
        const { uuid } = match.params;
        if (uuid) {
            const fetchData = async () => {
                const result = await getStillingByUuid(uuid);
                setAd(result);
            };

            fetchData();
        }
    }, [match.params.uuid]);

    if (!ad) {
        return null;
    }
    return (
        <div>
            <div className="StillingJson">
                {ad.adText}
            </div>
        </div>
    );
};


Preview.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            uuid: PropTypes.string
        })
    }).isRequired
};

export default Preview;
