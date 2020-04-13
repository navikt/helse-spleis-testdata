const baseUrl = process.env.NODE_ENV === 'development' ? 'http://localhost:3000' : '';

export const Status = {
    Suksess: 'suksess',
    Sender: 'sender',
    Error: 'error'
};

export const getInntekt = async ({ fnr }) => {
    return await fetch(`${baseUrl}/person/inntekt`, {
        method: 'get',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            ident: fnr
        }
    });
};

export const getAktørId = async ({ fnr }) => {
    return await fetch(`${baseUrl}/person/aktorid`, {
        method: 'get',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            ident: fnr
        }
    });
};

export const deletePerson = async ({ fnr }) => {
    return await fetch(`${baseUrl}/person`, {
        method: 'delete',
        headers: { ident: fnr }
    });
};

export const postVedtaksperiode = async ({ vedtak }) => {
    return await fetch(`${baseUrl}/vedtaksperiode/`, {
        method: 'post',
        body: JSON.stringify(vedtak),
        headers: { 'Content-Type': 'application/json' }
    });
};