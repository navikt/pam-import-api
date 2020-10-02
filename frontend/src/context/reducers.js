import moment from 'moment';

const initialState = {
    ad: ''
};

const types = {
    FETCH_AD_RESULT: 'FETCH_AD_RESULT',
};

const reducer = (state = initialState, action) => {
    switch (action.type) {
        case types.FETCH_AD_RESULT:
            return {
                ...state,
                ad: action.value
            };
        default:
            return state;
    }
};
export { initialState, types, reducer };
