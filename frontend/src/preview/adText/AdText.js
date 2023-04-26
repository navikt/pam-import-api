import React from 'react';
import PropTypes from 'prop-types';
import parse from 'html-react-parser';
import './AdText.less';

export default function AdText({adText}) {
    if (adText) {
        return (
            <section className="AdText">
                {parse(adText)}
            </section>
        );
    }
    return null;
}

AdText.defaultProps = {
    adText: undefined
};

AdText.propTypes = {
    adText: PropTypes.string
};
