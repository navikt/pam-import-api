import React from 'react';
import PropTypes from 'prop-types';
import { Undertittel } from 'nav-frontend-typografi';

export default function ContactPerson({ contactList }) {
    if (contactList && contactList.length > 0) {
        return (
            <div className="detail-section">
                <Undertittel className="detail-section__head">Kontaktperson for stillingen</Undertittel>
                <div className="detail-section__body">
                    <dl className="dl-flex typo-normal">
                        {contactList[0].name && [
                            <dt key="dt">Kontaktperson:</dt>,
                            <dd key="dd">{contactList[0].name}</dd>
                        ]}
                        {contactList[0].title && [
                            <dt key="dt">Stillingstittel:</dt>,
                            <dd key="dd">{contactList[0].title}</dd>
                        ]}
                        {contactList[0].phone && [
                            <dt key="dt">Telefon:</dt>,
                            <dd key="dd">{contactList[0].phone}</dd>
                        ]}
                        {contactList[0].email && [
                            <dt key="dt">Epost:</dt>,
                            <dd key="dd">
                                <a className="link" rel="nofollow" href={`mailto:${contactList[0].email}`}>{contactList[0].email}</a>
                            </dd>
                        ]}
                    </dl>
                </div>
            </div>
        );
    }
    return null;
}

ContactPerson.defaultProps = {
    contactList: undefined
};

ContactPerson.propTypes = {
    contactList: PropTypes.arrayOf(PropTypes.shape({
        person: PropTypes.string,
        title: PropTypes.string,
        phone: PropTypes.string,
        email: PropTypes.string
    }))
};
