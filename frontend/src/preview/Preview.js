import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import './Stilling.less';
import {getStillingByUuid} from "../api/api";
import {getStillingByProviderIdReference} from "../api/api";
import { Column, Container, Row } from 'nav-frontend-grid';
import AdText from './adText/AdText';
import HowToApply from "./howToApply/HowToApply";
import EmploymentDetails from "./employmentDetails/EmploymentDetails";
import ContactPerson from "./contactPerson/ContactPerson";
import EmployerDetails from "./employerDetails/EmployerDetails";
import AdDetails from "./adDetails/AdDetails";
import AlertStripe from 'nav-frontend-alertstriper';

const Preview = ({ match }) => {
    const [ad, setAd] = useState(undefined);
    useEffect(() => {
        let uuid = match.params.uuid;
        if (uuid) {
            const fetchData = async () => {
                const result = await getStillingByUuid(uuid);
                setAd(result);
            };

            fetchData();
        }
        let providerId = match.params.providerId
        let reference = match.params.reference
        if (providerId && reference) {
            const fetchData = async () => {
                const result = await getStillingByProviderIdReference(providerId, reference);
                setAd(result);
            };
            fetchData()
        }
    }, []);

    if (!ad) {
        return null;
    }

    return (
        <div className="Stilling">
            <Container>
                <Row>
                    <Column xs='12' md='7' lg='8'>
                        <div className="Stilling__left">
                            {ad.status === 'DELETED' || ad.status === 'STOPPED' && (
                                <AlertStripe type="advarsel" className="Expired alertstripe--solid">
                                    Stillingsannonsen er inaktiv.
                                </AlertStripe>
                            )}
                                <React.Fragment>
                                    <h1 className="Stilling__h1">{ad.title}</h1>
                                </React.Fragment>
                                <React.Fragment>
                                    <AdText adText={ad.adText}/>
                                </React.Fragment>
                        </div>
                    </Column>
                    <Column xs='12' md='5' lg='4'>
                            <React.Fragment>
                                <HowToApply properties={ad.properties} />
                                <EmploymentDetails stilling={ad} />
                                <ContactPerson contactList={ad.contactList} />
                                <EmployerDetails stilling={ad} />
                                <AdDetails ad={ad} />
                            </React.Fragment>
                    </Column>
                </Row>
            </Container>
        </div>
    );
};


Preview.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            uuid: PropTypes.string,
            providerId: PropTypes.string,
            reference: PropTypes.string
        })
    }).isRequired
};

export default Preview;
