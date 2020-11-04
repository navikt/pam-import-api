import React from 'react';
import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';
import './AdText.less';

export default function AdText({adText}) {
    if (adText) {
        return (
            <section className="AdText">
                {ReactHtmlParser(adText)}
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
