import React from 'react';
import PropTypes from 'prop-types';
import { Undertittel } from 'nav-frontend-typografi';
import { formatISOString } from '../../utils';

export default function AdDetails({ ad }) {
    return (
        <div className="AdDetails detail-section">
            <Undertittel className="AdDetails__head detail-section__head">Om annonsen</Undertittel>
            <div className="detail-section__body">
                <dl className="dl-flex typo-normal">
                    {ad.updated && [
                        <dt key="dt">Sist endret:</dt>,
                        <dd key="dd">{formatISOString(ad.updated, 'DD.MM.YYYY')}</dd>
                    ]}
                    {ad.medium && [
                        <dt key="dt">Hentet fra:</dt>,
                        <dd key="dd">{ad.medium}</dd>
                    ]}
                    {ad.reference && [
                        <dt key="dt">Referanse:</dt>,
                        <dd key="dd">{ad.reference}</dd>
                    ]}
                    {ad.id && [
                        <dt key="dt">Stillingsnummer:</dt>,
                        <dd key="dd">{ad.id}</dd>
                    ]}
                </dl>
            </div>
        </div>
    );
}

AdDetails.propTypes = {
    ad: PropTypes.shape({
        updated: PropTypes.string,
        medium: PropTypes.string,
        reference: PropTypes.string,
        id: PropTypes.number
    }).isRequired
};

