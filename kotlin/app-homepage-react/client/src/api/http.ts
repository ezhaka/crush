
export async function httpGet(path: string, token: string) {
    return await httpRequest('GET', token, path);
}

export async function httpPost(path: string, token: string, body: object) {
    return await httpRequest('POST', token, path, body);
}

async function httpRequest(method: string, token: string, path: string, body?: object) {
    let requestBody = undefined;
    let requestHeaders: HeadersInit = {Authorization: `Bearer ${token}`}

    if (body !== undefined) {
        requestBody = JSON.stringify(body)
        requestHeaders['Accept'] = 'application/json';
        requestHeaders['Content-Type'] = 'application/json';
    }

    let requestInit: RequestInit = {
        method: method,
        headers: requestHeaders,
        body: requestBody
    }

    return await fetch(path, requestInit)
}
