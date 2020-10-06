import React from 'react';
import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';
import { Undertittel } from 'nav-frontend-typografi';
import fixLocationName from '../employmentDetails/fixLocationName';
import { isValidUrl } from '../../utils';
import getEmployer from './getEmployer';
import './EmployerDetails.less';

function getEmployerLocation(employer) {
    let employerLocation = null;

    if (employer && employer.location) {
        const { location } = employer;

        if (location.postalCode) {
            employerLocation = location.address ? `${location.address}, ` : '';
            employerLocation += `${location.postalCode} ${fixLocationName(location.city)}`;
        } else if (location.municipal) {
            employerLocation = `${fixLocationName(location.municipal)}`;
        } else if (location.country) {
            employerLocation = `${fixLocationName(location.country)}`;
        }
    }
    return employerLocation;
}

export default function EmployerDetails({ stilling }) {
    const { properties } = stilling;
    const employer = getEmployer(stilling);
    const employerLocation = getEmployerLocation(stilling.employer);
    return (
        <div className="EmployerDetails detail-section">
            <Undertittel className="EmployerDetails__head detail-section__head">Om arbeidsgiveren</Undertittel>
            <div className="detail-section__body">
                <dl className="dl-flex typo-normal">
                    {employer && [
                        <dt key="dt">Arbeidsgiver:</dt>,
                        <dd key="dd">{employer}</dd>
                    ]}
                    {employerLocation && [
                        <dt key="dt">Adresse:</dt>,
                        <dd key="dd">{employerLocation}</dd>
                    ]}
                    {properties.employerhomepage && [
                        <dt key="dt">Hjemmeside:</dt>,
                        <dd key="dd">
                            {isValidUrl(properties.employerhomepage) ? (
                                <a
                                    href={properties.employerhomepage}
                                    className="link"
                                >
                                    {properties.employerhomepage}
                                </a>)
                                : properties.employerhomepage
                            }
                        </dd>
                    ]}
                    {properties.linkedinpage && [
                        <dt key="dt">LinkedIn:</dt>,
                        <dd key="dd">
                            {isValidUrl(properties.linkedinpage) ? (
                                <a
                                    href={properties.linkedinpage}
                                    className="link"
                                >
                                    {properties.linkedinpage}
                                </a>)
                                : properties.linkedinpage
                            }
                        </dd>
                    ]}
                    {properties.twitteraddress && [
                        <dt key="dt">Twitter:</dt>,
                        <dd key="dd">
                            {isValidUrl(properties.twitteraddress) ? (
                                <a
                                    href={properties.twitteraddress}
                                    className="link"
                                >
                                    {properties.twitteraddress}
                                </a>)
                                : properties.twitteraddress
                            }
                        </dd>
                    ]}
                    {properties.facebookpage && [
                        <dt key="dt">Facebook:</dt>,
                        <dd key="dd">
                            {isValidUrl(properties.facebookpage) ? (
                                <a
                                    href={properties.facebookpage}
                                    className="link"
                                >
                                    {properties.facebookpage}
                                </a>)
                                : properties.facebookpage
                            }
                        </dd>
                    ]}
                </dl>
                {properties.employerdescription && (
                    <div className="EmployerDetails__description">{ ReactHtmlParser(properties.employerdescription) }</div>
                )}
            </div>
        </div>
    );
}

EmployerDetails.propTypes = {
    stilling: PropTypes.shape({
        properties: PropTypes.shape({
            employer: PropTypes.string,
            address: PropTypes.string,
            employerhomepage: PropTypes.string,
            linkedinpage: PropTypes.string,
            twitteraddress: PropTypes.string,
            facebookpage: PropTypes.string,
            employerdescription: PropTypes.string
        }).isRequired
    }).isRequired
};
