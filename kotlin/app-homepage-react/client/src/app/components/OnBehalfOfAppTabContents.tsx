import WarningBox from "./WarningBox";
import {AppTabContents} from "./AppTabContents";
import fetchSpaceUserToken, {
    appPermissionScope,
    requestAppPermissions,
    requestUserToken,
    UserTokenData
} from "../service/spaceAuth";
import Loader from "./Loader";
import {useState as useAsyncState} from "@hookstate/core";
import {useState} from "react";
import {onBehalfOfTheAppApiImpl} from "../service/onBehalfOfTheAppApiImpl";
import {httpGet} from "../service/utils";
import AsyncSelect from "react-select/async";
import * as utils from "../service/utils";
import {Theme} from "react-select";
import * as theme from "../service/theme";
import {ChatChannel} from "../service/chatChannel";

interface OnBehalfOfAppTabContentsState {
    userTokenData?: UserTokenData;
    appHasPermissions: boolean;
}

export function OnBehalfOfAppTabContents() {
    const state = useAsyncState(() => loadInitialTabState());

    if (state.promised) {
        return (<Loader/>);
    }

    return (
        <>
            {
                !state.appHasPermissions.get() &&
                <WarningBox text="Authorize the app to view channels and send messages on behalf of itself"
                            isActionable={true}
                            onAction={() => requestAppPermissions(
                                appPermissionScope,
                                (success: boolean) => {
                                    if (success) {
                                        state.appHasPermissions.set(true);
                                    }
                                }
                            )}
                />
            }


            <AppTabContents
                key="OnBehalfOfTheApp"
                userTokenData={state.userTokenData.get()}
                apiImpl={onBehalfOfTheAppApiImpl}
                implementationNote="HTTP requests are made from the iframe to the app server, which then makes requests to Space with application access token."
                sourceCodeHRef="https://github.com/JetBrains/space-app-tutorials/blob/main/kotlin/app-homepage-react/client/src/app/service/onBehalfOfTheAppApiImpl.ts"
                sourceCodeLinkText="onBehalfOfTheAppApiImpl.ts"
            />
        </>
    )
}

function loadInitialTabState(): Promise<OnBehalfOfAppTabContentsState> {
    return new Promise(async (resolve) => {
        const userTokenData = await fetchSpaceUserToken(false, "");
        if (userTokenData === undefined) {
            throw "Could not get userToken with empty permissionScope";
        }

        const appHasPermissionsResponseRaw = await httpGet("/homepage/app-has-permissions", userTokenData.userToken);
        const appHasPermissionsResponse = (await appHasPermissionsResponseRaw.json()) as AppHasPermissionsResponse

        requestUserToken(false, "", (newUserTokenData) => {
            resolve({
                userTokenData: newUserTokenData,
                appHasPermissions: appHasPermissionsResponse.hasPermissions,
            })
        });
    });
}

interface AppHasPermissionsResponse {
    hasPermissions: boolean
}