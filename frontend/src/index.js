import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Preview from './preview/Preview';
import { StoreProvider } from './context/StoreContext';
import './styles.less';


const Index = () => {
    return (
        <main>
            <Routes>
                <Route exact path={`${__CONTEXT_PATH__}/:uuid`} element={<Preview />}/>
            </Routes>
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
