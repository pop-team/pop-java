parclass Integer
{
	classuid(1000);
	
public:
	Integer();
	~Integer();
	mutex void Add (Integer &other);
	conc int Get ();
	seq async void Set(int val);
	
private:
	int data;
};

