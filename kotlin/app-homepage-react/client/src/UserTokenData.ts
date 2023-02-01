

export interface UserTokenData {
    token: string;
    serverUrl: string;
}

export async function fetchSpaceUserToken(askForConsent: boolean = false, permissionScope: string = ""): Promise<UserTokenData | undefined> {
    // read more about getting user token in Space documentation:
    // https://www.jetbrains.com/help/space/application-homepage.html#getusertokenrequest-get-space-user-token

    return await new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => {
            resolve(e.data);
        }
        window.parent.postMessage({
            type: "GetUserTokenRequest",
            permissionScope: permissionScope,
            askForConsent: askForConsent
        }, "*", [channel.port2]);
    }) as UserTokenData
}

export const permissionScope = "global:Profile.View"

export function approvePermissionRequest() {
    return new Promise(resolve => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({
            type: "ApprovePermissionsRequest",
            permissionScope: permissionScope,
        }, "*", [channel.port2]);
    })
}
