def map_data(data):
    result = []
    for single_record in data:
        for point in single_record:
            result.append((point, 1))
    return result

def shuffleGroupBy(mappedData):
    records = {}
    for word, count in mappedData:
        if word in records:
            records[word].append(count)
        else:
            records[word] = [count]
    return records

def reduce_data(shuffledData):
    reducedResult = {}
    for word, counts in shuffledData:
        reducedResult[word] = sum(counts)
    return reducedResult

def mapReduce(data, workers):

    chunk_size = len(data) // workers
    print(chunk_size)
    chunks = [data[i, i+chunk_size] for i in range(0, len(data), chunk_size)]

    mapped_data = []
    for point in chunks:
        mapIt = map_data(point)
        mapped_data.append(mapIt)
    
    shuffleOutput = []

    for point in mapped_data:
        result = shuffleGroupBy(point)
        shuffleOutput.append(result)
    
    reduceData = []

    for point in shuffledData:
        reduceData.append(reduce_data(point))

    print(mapped_data, shuffledData, reduceData)

if __name__ == "__main__":
    data =["Hi there", "This is cool", "This is Awesome", "Ironman"]
    workers = 2
    mapReduce(data, workers)