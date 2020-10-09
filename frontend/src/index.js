import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route, Switch } from 'react-router-dom';
import Preview from './preview/Preview';
import { StoreProvider } from './context/StoreContext';
import './styles.less';


const Index = () => {
    return (
        <main>
            <Switch>
                <Route exact path={`${__CONTEXT_PATH__}/:uuid`} component={Preview} />
            </Switch>
        </main>

    );
};

ReactDOM.render(
    <StoreProvider>
        <BrowserRouter>
            <Index />
        </BrowserRouter>
    </StoreProvider>,
    document.getElementById('app')
);
