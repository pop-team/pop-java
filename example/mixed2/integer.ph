parclass Integer
{
	classuid(1000);
	
public:
	Integer();
	~Integer();
	mutex sync void Add ([in]Integer &other);
	conc int Get ();
	seq async void Set(int val);
	
private:
	int data;
};

