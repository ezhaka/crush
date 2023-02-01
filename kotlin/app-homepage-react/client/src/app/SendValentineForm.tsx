import * as React from "react";
import {UserTokenData} from "../UserTokenData";
import {useContext, useState} from "react";
import {httpGet, httpPost} from "../api/http";
import AsyncSelect from "react-select/async";
import "./SendValentineForm.css";
import {PageContext} from "./App";

type Props = {
    token: UserTokenData;
}

interface ProfileSelectItem extends Profile {
    value: string;
    label: string;
}

export const SendValentineForm = ({token}: Props) => {
    const [profile, setProfile] = useState<Profile>()
    const [message, setMessage] = useState<string>()
    const setPage = useContext(PageContext)

    const loadOptions = async (
        inputValue: string,
    ) => {
        const responseRaw = await httpGet(`/homepage/get-profiles?query=${inputValue || ''}`, token.token)
        const response = await responseRaw.json() as ProfileListResponse
        return response.data.map(p => ({...p, value: p.id, label: `${p.firstName} ${p.lastName}`}))
    };

    const submit = () => {
        // TODO: all to body!
        httpPost(`/homepage/send-valentine?receiverId=${profile?.id}&messageText=${message}`, token.token, {})
            .then(() => console.log("posted!"))
    }

    return (
        <>
            <div className="send-valentine-form">
                {"Send Valentine"}

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
                <button onClick={() => setPage({kind:"root"})}>X</button>
            </div>
        </>
    )
}