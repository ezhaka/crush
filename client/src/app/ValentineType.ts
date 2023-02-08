export interface ValentineType {
    name: string;
    textColor: string;
    left: number;
    top: number;
    width: number;
    height: number;
}

export const valentineTypes: ValentineType[] = [
    {
        name: 'piggy',
        textColor: '#2D0F13',
        left: 280,
        top: 220,
        width: 380,
        height: 160
    },
    {
        name: 'pill',
        textColor: 'white',
        left: 116,
        top: 290,
        width: 380,
        height: 160,
    },
    {
        name: 'chocolate',
        textColor: 'white',
        left: 180,
        top: 300,
        width: 296,
        height: 190,
    },
    {
        name: 'cloud',
        textColor: '#3B467A',
        left: 474,
        top: 244,
        width: 296,
        height: 190,
    },
    {
        name: 'match',
        textColor: 'white',
        left: 360,
        top: 210,
        width: 380,
        height: 160,
    }
]

