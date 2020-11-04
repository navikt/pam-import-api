import { types } from './reducers';

export const useActions = (state, dispatch) => {
    function fetchAd(value) {
        dispatch({ type: types.FETCH_AD_RESULT, value });
    }

    return {
        fetchAd
    };
};
