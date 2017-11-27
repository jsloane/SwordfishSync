
export interface Api<T> {
    content: T[];
    first: boolean;
    last: boolean;
    number: number;
    numberOfElements: number;
    size: number;
    sort: ApiSort[];
    totalElements: number;
    totalPages: number;
}

interface ApiSort {
    ascending: boolean;
    descending: boolean;
    direction: string;
    ignoreCase: boolean;
    nullHandling: string;
    property: string;
}
