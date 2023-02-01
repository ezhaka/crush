import "./AppTabContents.css";
import {SpaceChannelSelection} from "./SpaceChannelSelection";
import {useState as useAsyncState} from "@hookstate/core";
import {useState} from "react";
import {UserTokenData} from "../service/spaceAuth";
import {SendMessageSection} from "./SendMessageSection";
import {ChatChannel, copyChannel} from "../service/chatChannel";
import {TabApiImplementation} from "./AppTabs";
import {httpPost, openInNewTab} from "../service/utils";
import AsyncSelect from "react-select/async";
import * as utils from "../service/utils";
import {components, Theme, ValueContainerProps} from "react-select";
import * as theme from "../service/theme";
import {getProfilesListImpl} from "../service/onBehalfOfTheAppApiImpl";
import {OptionProps} from "react-select/dist/declarations/src/components/Option";
import {ChannelIcon} from "./ChannelIcon";

export interface AppTabContentsProps {
    userTokenData?: UserTokenData;
    apiImpl: TabApiImplementation;
    implementationNote: string;
    sourceCodeHRef: string;
    sourceCodeLinkText: string;
}

interface AppTabContentsState {
    selectedChannel?: ChatChannel;
}

const Option = (props: OptionProps<Profile>) => {
    let classNames = props.isSelected || props.isFocused ? "selectOption selectOptionFocused" : "selectOption selectOptionNotFocused";
    return (
        <div className={classNames}>
            <components.Option {...props}/>
        </div>
    );
};

type IsMulti = false;

const ValueContainer = ({
                            children,
                            ...props
                        }: ValueContainerProps<Profile, IsMulti>) => {
    const value = props.selectProps.value as Profile;

    return (
        <div className="selectOption">
            <components.ValueContainer {...props}>{children}</components.ValueContainer>
        </div>
    )
};

interface ProfileSelectItem extends Profile {
    value: string;
    label: string;
}

export function AppTabContents(props: AppTabContentsProps) {
    const state = useAsyncState({
        selectedChannel: undefined
    } as AppTabContentsState);

    const [profile, setProfile] = useState<Profile>()
    const [message, setMessage] = useState<string>()

    const loadOptions = async (
        inputValue: string,
    ) => {
        const resp = await getProfilesListImpl(inputValue, props.userTokenData)
        return resp.data.map(p => ({...p, value: p.id, label: `${p.firstName} ${p.lastName}`}))
    };

    const submit = () => {
        const token = props.userTokenData?.userToken;

        if (token) {
            // TODO: all to body!
            httpPost(`/homepage/send-valentine?receiverId=${profile?.id}&messageText=${message}`, token, {})
                .then(() => console.log("posted!"))
        }
    }

    return (
        <>
            <div className="app-tab-contents">
                <AsyncSelect
                    cacheOptions
                    defaultOptions
                    // placeholder="Type to filter people"
                    loadOptions={loadOptions}
                    // components={{Option, ValueContainer}}
                    onChange={(newValue) => setProfile(newValue as ProfileSelectItem)}
                    value={profile}
                />

                <textarea value={message} onChange={(e) => setMessage(e.target.value)} rows={5}/>

                <button onClick={submit}>SEND</button>
            </div>
        </>
    )
}
