import React from 'react';
import PropTypes from 'prop-types';
import { Undertittel, Undertekst } from 'nav-frontend-typografi';
import {formatISOString, isValidEmail, isValidISOString, isValidUrl} from '../../utils';
import './HowToApply.less';

export function getApplicationUrl(properties) {
    if (properties.applicationurl !== undefined) {
        return properties.applicationurl;
    }
    return properties.sourceurl;
}

export default function HowToApply({properties }) {
    const sokUrl = getApplicationUrl(properties);
    if (properties.applicationdue || properties.applicationemail || sokUrl) {
        return (
            <div className="HowToApply detail-section">
                <Undertittel className="HowToApply__head detail-section__head">Søknad</Undertittel>
                <div className="detail-section__body">
                    <dl className="dl-flex typo-normal">
                        {properties.applicationdue && [
                            <dt key="dt">Søknadsfrist:</dt>,
                            <dd key="dd">
                                {isValidISOString(properties.applicationdue) ?
                                    formatISOString(properties.applicationdue, 'DD.MM.YYYY') :
                                    properties.applicationdue}
                            </dd>
                        ]}
                        {properties.applicationemail && [
                            <dt key="dt">Send søknad til:</dt>,
                            <dd key="dd">{
                                isValidEmail(properties.applicationemail) ?
                                    <a className="link" href={`mailto:${properties.applicationemail}`}>
                                        {properties.applicationemail}
                                    </a>
                                    : properties.applicationemail
                            }</dd>
                        ]}
                        {sokUrl && !isValidUrl(sokUrl) && [
                            <dt key="dt">Søknadslenke:</dt>,
                            <dd key="dd">{sokUrl}</dd>
                        ]}
                    </dl>

                    {sokUrl && isValidUrl(sokUrl) && (
                        <div className="HowToApply__send-button-wrapper">
                            <a
                                onClick={() => {
                                    const eventLabel = `sok-pa-stillingen`;
                                }}
                                className="HowToApply__send-button Knapp Knapp--hoved blokk-xxs"
                                href={sokUrl}
                            >
                                <div className="HowToApply__send-button-content">
                                    <span className="HowToApply__send-button-icon" />Søk på stillingen
                                </div>
                            </a>
                        </div>)
                    }
                </div>
            </div>
        );
    }
    return null;
}

HowToApply.propTypes = {
    properties: PropTypes.shape({
        applicationdue: PropTypes.string,
        applicationemail: PropTypes.string,
        applicationurl: PropTypes.string,
        sourceurl: PropTypes.string
    }).isRequired
};

