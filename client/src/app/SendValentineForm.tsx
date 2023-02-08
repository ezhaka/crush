import * as React from "react";
import {useContext, useState} from "react";
import {UserTokenData} from "../UserTokenData";
import {httpPost} from "../api/http";
import "./Common.css"
import "./SendValentineForm.css";
import {ProfileSelectItem, ProfileSelector} from "./ProfileSelector";
import {Button} from "./Button";
import {valentineTypes} from "./ValentineType";
import {ValentineEditor} from "./ValentineEditor";
import {CloseableOverlay} from "./CloseableOverlay";
import {PageContext} from "./App";

type Props = {
    token: UserTokenData;
}

export const SendValentineForm = ({token}: Props) => {
    const [profile, setProfile] = useState<ProfileSelectItem>()
    const [message, setMessage] = useState<string>('')
    const [posted, setPosted] = useState<boolean>(false)
    const [shakeEmpty, setShakeEmpty] = useState<boolean>(false)

    const submit = () => {
        if (!profile) {
            if (!shakeEmpty) {
                setShakeEmpty(true)
                setTimeout(() => setShakeEmpty(false), 1000)
            }
        } else {
            // TODO: all to body!
            httpPost(`/homepage/send-valentine`, token.token, {
                receiverId: profile?.id,
                messageText: message
            })
                .then((response) => {
                    if (response.ok) {
                        setPosted(true)
                    } else {
                        // TODO: show error toast
                    }
                })
        }
    }

    return (
        <CloseableOverlay>
            {!posted && <div className="send-valentine-form">
                <div className={shakeEmpty && 'shake-empty-dropdown'}>
                    <ProfileSelector value={profile} onChange={setProfile} token={token}/>
                </div>
                <ValentineEditor message={message} setMessage={setMessage} type={valentineTypes[0]}/>
                <Button title="SEND IT!" action={submit}/>
            </div>}

            {posted && <ValentineIsSent/>}
        </CloseableOverlay>
    )
}

const kotikSrc = require(`./../../resources/kotik.png`)

const ValentineIsSent = () => {
    const setPage = useContext(PageContext)

    return (<div className="valentine-is-sent">
        <div className="logo"/>
        <div className="promo-header">Your Secret Valentine Has Been Sent!</div>
        <div className="promo-text">
            It will be delivered anonymously to your crush. We hope that this Valentine's Day will be filled with joy
            and surprises for both of you.
            <br/>
            <br/>
            Wishing you all the best on this day of love!
        </div>
        <Button title="OK!" action={() => {
            setPage({kind: "root"})
        }}/>
        <img src={kotikSrc} className="kotik"/>
    </div>)
}
