interface Profile {
    id: string;
    firstName: string;
    lastName: string;
}

interface ProfileListResponse {
    data: Profile[]
}

interface Valentine {
    id: number;
    message: string;
    type: number;
    read: boolean;
}