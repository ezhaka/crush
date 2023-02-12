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
import {ValentineCarousel} from "./ValentineCarousel";

type Props = {
    token: UserTokenData;
}

type PostingState = 'posting' | 'posting_too_long' | 'posted' | 'error'

export const SendValentineForm = ({token}: Props) => {
    const setPage = useContext(PageContext)
    const [profile, setProfile] = useState<ProfileSelectItem>()
    const [message, setMessage] = useState<string>('')
    const [postingState, setPostingState] = useState<PostingState>(undefined)
    const [shakeEmpty, setShakeEmpty] = useState<boolean>(false)
    const [activeSlide, setActiveSlide] = useState(0);

    const submit = () => {
        if (!!postingState) {
            return
        }

        if (!profile) {
            if (!shakeEmpty) {
                setShakeEmpty(true)
                setTimeout(() => setShakeEmpty(false), 1000)
            }
        } else {
            setPostingState('posting')

            const timer = setTimeout(() => setPostingState('posting_too_long'), 1000)

            httpPost(`/api/send-valentine`, token.token, {
                receiverId: profile?.id,
                messageText: message,
                cardType: activeSlide
            })
                .then((response) => {
                    clearTimeout(timer)
                    if (response.ok) {
                        setPostingState('posted')
                    } else {
                        setPostingState('error')
                    }
                })
                .catch(() => {
                    clearTimeout(timer)
                    setPostingState('error')
                })
        }
    }

    return (
        <CloseableOverlay>
            {(!postingState || postingState === 'posting') && <div className="send-valentine-form">
                <div className={shakeEmpty && 'shake-empty-dropdown'}>
                    <ProfileSelector value={profile} onChange={setProfile} token={token}/>
                </div>

                <ValentineCarousel
                    infinite={true}
                    slidesCount={valentineTypes.length}
                    activeSlide={activeSlide}
                    setActiveSlide={setActiveSlide}
                >
                    {valentineTypes.map((item, index) => (
                        <ValentineEditor key={index} message={message} setMessage={setMessage} type={item}/>
                    ))}
                </ValentineCarousel>

                <Button title="SEND IT!" action={submit}/>

                <div className="promo-text">It’s absolutely confidential. Your feelings will reach your crush without revealing your identity. We will not store this information anywhere.</div>
            </div>}

            {postingState === 'posting_too_long' && <div className="valentine-is-sent">
                <div className="logo"/>
                <div className="promo-header">Sending Love...</div>
                <div className="promo-text">But It's Taking A Bit Longer Than Expected</div>
            </div>}

            {postingState === 'posted' && <ValentineIsSent/>}

            {postingState === 'error' && <div className="valentine-is-sent">
                <div className="logo"/>
                <div className="promo-header">Oops!</div>
                <div className="promo-text">Love Delivery Failed. Please Try Again Later.</div>
                <Button title="OKAY" action={() => {
                    setPage({kind: "root"})
                }}/>
            </div>}
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
